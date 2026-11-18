import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize, forkJoin, of } from 'rxjs';
import {
  Catalogos,
  RequerimientoService
} from '../../../services/requerimiento.service';

@Component({
  selector: 'app-formulario',
  standalone: false,
  templateUrl: './formulario.html',
  styleUrl: './formulario.scss'
})
export class Formulario implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly servicio = inject(RequerimientoService);
  private readonly ruta = inject(ActivatedRoute);
  private readonly router = inject(Router);

  private id: number | null = null;

  readonly editando = signal(false);
  readonly catalogos = signal<Catalogos | null>(null);
  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly permitido = signal(false);
  readonly error = signal('');

  readonly formulario = this.fb.nonNullable.group({
    titulo: ['', [
      Validators.required,
      Validators.pattern(/\S/),
      Validators.maxLength(150)
    ]],
    descripcion: ['', [
      Validators.required,
      Validators.pattern(/\S/),
      Validators.minLength(10),
      Validators.maxLength(5000)
    ]],
    tipoRequerimientoId: [0, [Validators.min(1)]],
    prioridadId: [0, [Validators.min(1)]]
  });

  ngOnInit(): void {
    const parametro = this.ruta.snapshot.paramMap.get('id');

    if (parametro !== null) {
      this.id = Number(parametro);
      this.editando.set(true);

      if (!Number.isSafeInteger(this.id) || this.id <= 0) {
        this.error.set('El identificador del requerimiento no es válido.');
        this.cargando.set(false);
        return;
      }
    }

    forkJoin({
      catalogos: this.servicio.obtenerCatalogos(),
      requerimiento: this.id !== null
        ? this.servicio.buscarPorId(this.id)
        : of(null)
    }).pipe(
      finalize(() => this.cargando.set(false))
    ).subscribe({
      next: resultado => {
        const catalogos = resultado.catalogos;
        const item = resultado.requerimiento;

        this.catalogos.set(catalogos);

        const autorizado = item
          ? this.servicio.puedeModificar(catalogos, item)
          : this.servicio.puedeCrear(catalogos);

        this.permitido.set(autorizado);

        if (!autorizado) {
          this.error.set('No tienes permiso para esta operación o el requerimiento ya no está en estado REGISTRADO.');
          return;
        }

        if (item) {
          const tipo = catalogos.tipos.find(
            opcion => opcion.nombre === item.tipo
          );

          const prioridad = catalogos.prioridades.find(
            opcion => opcion.nombre === item.prioridad
          );

          this.formulario.setValue({
            titulo: item.titulo,
            descripcion: item.descripcion,
            tipoRequerimientoId: tipo?.id ?? 0,
            prioridadId: prioridad?.id ?? 0
          });

          if (!tipo || !prioridad) {
            this.error.set('El tipo o la prioridad anterior ya no está disponible. Selecciona una opción vigente.');
          }
        }
      },
      error: (respuesta: HttpErrorResponse) => {
        if (respuesta.status === 404) {
          this.error.set('El requerimiento no existe.');
        } else if (respuesta.status === 403) {
          this.error.set('No tienes permiso para acceder a este requerimiento.');
        } else {
          this.error.set('No se pudo cargar el formulario. Recarga la página.');
        }
      }
    });
  }

  guardar(): void {
    if (!this.permitido() || this.guardando() || this.cargando()) {
      return;
    }

    this.error.set('');

    const valores = this.formulario.getRawValue();

    this.formulario.patchValue({
      titulo: valores.titulo.trim(),
      descripcion: valores.descripcion.trim()
    });

    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.guardando.set(true);

    const datos = this.formulario.getRawValue();

    const solicitud = this.id !== null
      ? this.servicio.actualizar(this.id, datos)
      : this.servicio.registrar(datos);

    solicitud.pipe(
      finalize(() => this.guardando.set(false))
    ).subscribe({
      next: resultado => {
        this.router.navigate(['/requerimientos', resultado.id]);
      },
      error: (respuesta: HttpErrorResponse) => {
        if (respuesta.status === 403) {
          this.error.set('No tienes permiso para realizar esta operación.');
        } else if (respuesta.status === 409) {
          this.error.set('La operación no puede completarse. Comprueba el estado del requerimiento.');
        } else if (respuesta.status === 404) {
          this.error.set('El requerimiento o uno de los datos seleccionados ya no existe.');
        } else if (respuesta.status === 400) {
          this.error.set('Revisa los datos del formulario y comprueba que tu cuenta y su área estén habilitadas.');
        } else {
          this.error.set('No se pudieron guardar los cambios. Comprueba la conexión.');
        }
      }
    });
  }
}
