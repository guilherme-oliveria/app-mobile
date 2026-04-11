// src/app/core/interceptors/error.interceptor.ts
import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ErrorResponse } from '../../shared/models/models';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const snackBar = inject(MatSnackBar);

  return next(req).pipe(
    catchError((httpError: HttpErrorResponse) => {
      const body = httpError.error as ErrorResponse | null;
      let mensagem = 'Erro inesperado. Tente novamente.';

      if (body?.mensagem) {
        // Backend retornou ErrorResponse padronizado
        mensagem = body.mensagem;

        // Se houver campos inválidos, monta a lista
        if (body.campos?.length) {
          const detalhe = body.campos.map(c => `${c.campo}: ${c.mensagem}`).join('\n');
          mensagem = `${mensagem}\n${detalhe}`;
        }
      } else {
        // Fallback por status HTTP
        switch (httpError.status) {
          case 0:
            mensagem = 'Sem conexão com o servidor';
            break;
          case 400:
            mensagem = 'Dados inválidos';
            break;
          case 401:
            mensagem = 'Sessão expirada — faça login novamente';
            break;
          case 403:
            mensagem = 'Você não tem permissão para esta ação';
            break;
          case 404:
            mensagem = 'Recurso não encontrado';
            break;
          case 409:
            mensagem = 'Conflito — registro duplicado ou concorrência';
            break;
          case 422:
            mensagem = 'Erro de regra de negócio';
            break;
          case 500:
            mensagem = 'Erro interno do servidor';
            break;
        }
      }

      snackBar.open(mensagem, 'Fechar', {
        duration: 6000,
        horizontalPosition: 'center',
        verticalPosition: 'top',
        panelClass: ['snackbar-erro']
      });

      return throwError(() => httpError);
    })
  );
};

