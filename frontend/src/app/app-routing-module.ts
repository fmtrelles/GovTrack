import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { Login } from './pages/login/login';
import { Listado } from './pages/requerimientos/listado/listado';
import { Formulario } from './pages/requerimientos/formulario/formulario';
import { Detalle } from './pages/requerimientos/detalle/detalle';
import { Layout } from './layout/layout';
import { authGuard } from './services/auth.guard';

const routes: Routes = [
  {
    path: 'login',
    component: Login
  },
  {
    path: '',
    component: Layout,
    canActivate: [authGuard],
    canActivateChild: [authGuard],
    children: [
      {
        path: 'requerimientos/nuevo',
        component: Formulario
      },
      {
        path: 'requerimientos/:id/editar',
        component: Formulario
      },
      {
        path: 'requerimientos/:id',
        component: Detalle
      },
      {
        path: 'requerimientos',
        component: Listado
      },
      {
        path: '',
        redirectTo: 'requerimientos',
        pathMatch: 'full'
      }
    ]
  },
  {
    path: '**',
    redirectTo: 'requerimientos'
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
