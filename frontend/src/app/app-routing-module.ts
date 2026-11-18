import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { Login } from './pages/login/login';
import { Listado } from './pages/requerimientos/listado/listado';
import { Formulario } from './pages/requerimientos/formulario/formulario';
import { Detalle } from './pages/requerimientos/detalle/detalle';
import { authGuard } from './services/auth.guard';

const routes: Routes = [
  { path: 'login', component: Login },
  {
    path: 'requerimientos/nuevo',
    component: Formulario,
    canActivate: [authGuard]
  },
  {
    path: 'requerimientos/:id/editar',
    component: Formulario,
    canActivate: [authGuard]
  },
  {
    path: 'requerimientos/:id',
    component: Detalle,
    canActivate: [authGuard]
  },
  {
    path: 'requerimientos',
    component: Listado,
    canActivate: [authGuard]
  },
  { path: '', redirectTo: 'requerimientos', pathMatch: 'full' },
  { path: '**', redirectTo: 'requerimientos' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
