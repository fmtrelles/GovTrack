import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs';

interface LoginSolicitud {
  correo: string;
  password: string;
}

interface LoginRespuesta {
  token: string;
  tipo: string;
  correo: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);

  iniciarSesion(datos: LoginSolicitud) {
    this.cerrarSesion();

    return this.http.post<LoginRespuesta>('/api/auth/login', datos).pipe(
      tap(respuesta => {
        sessionStorage.setItem('govtrack_token', respuesta.token);
        sessionStorage.setItem('govtrack_correo', respuesta.correo);
      })
    );
  }

  obtenerToken(): string | null {
    return sessionStorage.getItem('govtrack_token');
  }

  cerrarSesion(): void {
    sessionStorage.removeItem('govtrack_token');
    sessionStorage.removeItem('govtrack_correo');
  }
}
