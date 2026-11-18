import { inject } from '@angular/core';
import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const protegida = request.url.startsWith('/api/')
    && !request.url.startsWith('/api/auth/');

  const token = auth.obtenerToken();

  const solicitud = protegida && token
    ? request.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    })
    : request;

  return next(solicitud).pipe(
    catchError((error: HttpErrorResponse) => {
      if (protegida && error.status === 401) {
        auth.cerrarSesion();
        router.navigateByUrl('/login');
      }

      return throwError(() => error);
    })
  );
};
