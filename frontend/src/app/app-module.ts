import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { ReactiveFormsModule } from '@angular/forms';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { Login } from './pages/login/login';
import { Listado } from './pages/requerimientos/listado/listado';
import { Formulario } from './pages/requerimientos/formulario/formulario';
import { Detalle } from './pages/requerimientos/detalle/detalle';
import { Layout } from './layout/layout';
import { authInterceptor } from './services/auth.interceptor';

@NgModule({
  declarations: [
    App,
    Login,
    Listado,
    Formulario,
    Detalle,
    Layout
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    ReactiveFormsModule
  ],
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideHttpClient(withInterceptors([authInterceptor]))
  ],
  bootstrap: [App]
})
export class AppModule {}
