import { Component, inject, OnInit, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize, forkJoin } from 'rxjs';
import {
  Requerimiento,
  RequerimientoService
} from '../../../services/requerimiento.service';

@Component({
  selector: 'app-detalle',
  standalone: false,
  templateUrl: './detalle.html',
  styleUrl: './detalle.scss'
})
export class Detalle implements OnInit {
  private readonly servicio = inject(RequerimientoService);
  private readonly ruta = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly requerimiento = signal<Requerimiento | null>(null);
  readonly cargando = signal(true);
  readonly eliminando = signal(false);
  readonly puedeModificar = signal(false);
  readonly error = signal('');

  ngOnInit(): void {
    const id = Number(this.ruta.snapshot.paramMap.get('id'));

    if (!Number.isSafeInteger(id) || id <= 0) {
      this.error.set('El identificador del requerimiento no es válido.');
      this.cargando.set(false);
      return;
    }

    forkJoin({
      usuario: this.servicio.obtenerCatalogos(),
      requerimiento: this.servicio.buscarPorId(id)
    }).pipe(
      finalize(() => this.cargando.set(false))
    ).subscribe({
      next: resultado => {
        this.requerimiento.set(resultado.requerimiento);

        this.puedeModificar.set(
          this.servicio.puedeModificar(
            resultado.usuario,
            resultado.requerimiento
          )
        );
      },
      error: (respuesta: HttpErrorResponse) => {
        if (respuesta.status === 404) {
          this.error.set('El requerimiento no existe.');
        } else if (respuesta.status === 403) {
          this.error.set('No tienes permiso para consultar este requerimiento.');
        } else {
          this.error.set('No se pudo cargar el requerimiento.');
        }
      }
    });
  }

  eliminar(): void {
    const item = this.requerimiento();

    if (!item || !this.puedeModificar() || this.eliminando()) {
      return;
    }

    const confirmado = window.confirm(
      `¿Deseas eliminar el requerimiento ${item.codigo}? Esta acción no se puede deshacer.`
    );

    if (!confirmado) {
      return;
    }

    this.error.set('');
    this.eliminando.set(true);

    this.servicio.eliminar(item.id).pipe(
      finalize(() => this.eliminando.set(false))
    ).subscribe({
      next: () => {
        this.router.navigateByUrl('/requerimientos');
      },
      error: (respuesta: HttpErrorResponse) => {
        if (respuesta.status === 403) {
          this.error.set('No tienes permiso para eliminar este requerimiento.');
        } else if (respuesta.status === 409) {
          this.error.set('El requerimiento ya no puede eliminarse en su estado actual.');
        } else if (respuesta.status === 404) {
          this.error.set('El requerimiento ya no existe.');
        } else {
          this.error.set('No se pudo eliminar el requerimiento.');
        }
      }
    });
  }
}
