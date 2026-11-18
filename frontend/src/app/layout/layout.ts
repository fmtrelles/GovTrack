import { Component, inject, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../services/auth.service';
import {
  Catalogos,
  RequerimientoService
} from '../services/requerimiento.service';

@Component({
  selector: 'app-layout',
  standalone: false,
  templateUrl: './layout.html',
  styleUrl: './layout.scss'
})
export class Layout implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly servicio = inject(RequerimientoService);
  private readonly router = inject(Router);

  readonly usuario = signal<Catalogos | null>(null);
  readonly cargando = signal(false);
  readonly error = signal('');

  ngOnInit(): void {
    this.cargarUsuario();
  }

  cargarUsuario(): void {
    if (this.cargando()) {
      return;
    }

    this.error.set('');
    this.cargando.set(true);

    this.servicio.obtenerCatalogos().pipe(
      finalize(() => this.cargando.set(false))
    ).subscribe({
      next: datos => this.usuario.set(datos),
      error: () => {
        this.error.set('No se pudieron cargar los datos de tu cuenta.');
      }
    });
  }

  puedeCrear(): boolean {
    const usuario = this.usuario();

    return usuario !== null && this.servicio.puedeCrear(usuario);
  }

  roles(): string {
    const nombres: Record<string, string> = {
      SOLICITANTE: 'Solicitante',
      RESPONSABLE: 'Responsable',
      SUPERVISOR: 'Supervisor',
      ADMINISTRADOR: 'Administrador'
    };

    return this.usuario()?.roles
      .map(rol => {
        const nombre = rol.replace(/^ROLE_/, '');
        return nombres[nombre] ?? nombre;
      })
      .join(', ') ?? '';
  }

  cerrarSesion(): void {
    this.auth.cerrarSesion();
    this.router.navigateByUrl('/login');
  }
}
