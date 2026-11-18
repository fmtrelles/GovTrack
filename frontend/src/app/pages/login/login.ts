import { Component, inject, signal } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class Login {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly cargando = signal(false);
  readonly error = signal('');

  readonly formulario = this.fb.nonNullable.group({
    correo: ['', [
      Validators.required,
      Validators.email,
      Validators.maxLength(150)
    ]],
    password: ['', [
      Validators.required,
      Validators.maxLength(64)
    ]]
  });

  ingresar(): void {
    if (this.cargando()) {
      return;
    }

    this.error.set('');

    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.cargando.set(true);

    const datos = this.formulario.getRawValue();

    this.auth.iniciarSesion({
      correo: datos.correo.trim(),
      password: datos.password
    }).pipe(
      finalize(() => this.cargando.set(false))
    ).subscribe({
      next: () => {
        this.router.navigateByUrl('/requerimientos');
      },
      error: (respuesta: HttpErrorResponse) => {
        if (respuesta.status === 401) {
          this.error.set('Correo o contraseña incorrectos, o cuenta no habilitada.');
        } else {
          this.error.set('No se pudo iniciar sesión. Verifica que el servidor esté funcionando.');
        }
      }
    });
  }
}
