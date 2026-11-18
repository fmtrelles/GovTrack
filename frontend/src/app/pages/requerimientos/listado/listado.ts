import { Component, inject, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { finalize, forkJoin } from 'rxjs';
import { AuthService } from '../../../services/auth.service';
import {
  Catalogos,
  Requerimiento,
  RequerimientoService
} from '../../../services/requerimiento.service';

@Component({
  selector: 'app-listado',
  standalone: false,
  templateUrl: './listado.html',
  styleUrl: './listado.scss'
})
export class Listado implements OnInit {
  private readonly servicio = inject(RequerimientoService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly catalogos = signal<Catalogos | null>(null);
  readonly requerimientos = signal<Requerimiento[]>([]);
  readonly cargando = signal(false);
  readonly error = signal('');

  ngOnInit(): void {
    this.cargando.set(true);

    forkJoin({
      catalogos: this.servicio.obtenerCatalogos(),
      requerimientos: this.servicio.listar()
    }).pipe(
      finalize(() => this.cargando.set(false))
    ).subscribe({
      next: resultado => {
        this.catalogos.set(resultado.catalogos);
        this.requerimientos.set(resultado.requerimientos);
      },
      error: () => {
        this.error.set('No se pudo cargar la información. Intenta recargar la página.');
      }
    });
  }

  filtrar(estadoId: string): void {
    this.cargando.set(true);
    this.error.set('');

    this.servicio.listar(estadoId).pipe(
      finalize(() => this.cargando.set(false))
    ).subscribe({
      next: datos => this.requerimientos.set(datos),
      error: () => {
        this.requerimientos.set([]);
        this.error.set('No se pudo consultar los requerimientos.');
      }
    });
  }

  cerrarSesion(): void {
    this.auth.cerrarSesion();
    this.router.navigateByUrl('/login');
  }
  puedeCrear(): boolean {
    return this.catalogos()?.roles.some(rol =>
      ['SOLICITANTE', 'ADMINISTRADOR'].includes(
        rol.replace(/^ROLE_/, '')
      )
    ) ?? false;
  }
}
