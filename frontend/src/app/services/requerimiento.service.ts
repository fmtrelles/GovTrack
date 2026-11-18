import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';

export interface Opcion {
  id: number;
  nombre: string;
}

export interface Catalogos {
  usuarioId: number;
  nombre: string;
  roles: string[];
  tipos: Opcion[];
  prioridades: Opcion[];
  estados: Opcion[];
}

export interface RequerimientoSolicitud {
  titulo: string;
  descripcion: string;
  tipoRequerimientoId: number;
  prioridadId: number;
}

export interface Requerimiento {
  id: number;
  codigo: string;
  titulo: string;
  descripcion: string;
  tipo: string;
  prioridad: string;
  estado: string;
  areaSolicitanteId: number;
  areaSolicitante: string;
  solicitanteId: number;
  solicitante: string;
  areaResponsableId: number | null;
  areaResponsable: string | null;
  responsableId: number | null;
  responsable: string | null;
  fechaRegistro: string;
  fechaActualizacion: string | null;
  fechaLimiteAtencion: string | null;
  fechaCierre: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class RequerimientoService {
  private readonly http = inject(HttpClient);
  private readonly url = '/api/requerimientos';

  listar(estadoId: string = '') {
    let params = new HttpParams();

    if (estadoId) {
      params = params.set('estadoId', estadoId);
    }

    return this.http.get<Requerimiento[]>(this.url, { params });
  }

  obtenerCatalogos() {
    return this.http.get<Catalogos>('/api/catalogos');
  }

  buscarPorId(id: number) {
    return this.http.get<Requerimiento>(`${this.url}/${id}`);
  }

  registrar(datos: RequerimientoSolicitud) {
    return this.http.post<Requerimiento>(this.url, datos);
  }

  actualizar(id: number, datos: RequerimientoSolicitud) {
    return this.http.put<Requerimiento>(`${this.url}/${id}`, datos);
  }

  eliminar(id: number) {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

  puedeCrear(usuario: Catalogos): boolean {
    return usuario.roles.some(rol =>
      ['SOLICITANTE', 'ADMINISTRADOR'].includes(
        rol.replace(/^ROLE_/, '')
      )
    );
  }

  puedeModificar(
    usuario: Catalogos,
    requerimiento: Requerimiento
  ): boolean {
    const roles = usuario.roles.map(rol => rol.replace(/^ROLE_/, ''));

    const autorizado = roles.includes('ADMINISTRADOR')
      || (
        roles.includes('SOLICITANTE')
        && usuario.usuarioId === requerimiento.solicitanteId
      );

    return autorizado
      && requerimiento.estado.toUpperCase() === 'REGISTRADO';
  }
}
