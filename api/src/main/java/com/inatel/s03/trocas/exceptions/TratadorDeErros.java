package com.inatel.s03.trocas.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

/**
 * Converte exceções em respostas de erro no formato padrão RFC 9457 (Problem Details),
 * com Content-Type application/problem+json:
 *
 * <pre>
 * { "type": "about:blank", "title": "Not Found", "status": 404,
 *   "detail": "Troca 123 não encontrada", "instance": "/trocas/123",
 *   "code": "NOT_FOUND" }
 * </pre>
 *
 * "code" (e "errors", nos erros de validação) são extensões nossas, permitidas pela RFC.
 *
 * Estende ResponseEntityExceptionHandler, que já gera esse formato para os erros do próprio
 * Spring MVC (rota inexistente, método não suportado...) com o status HTTP correto.
 */
@Slf4j
@RestControllerAdvice
public class TratadorDeErros extends ResponseEntityExceptionHandler {

    // ---------- Exceções da aplicação ----------

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ProblemDetail naoEncontrado(RecursoNaoEncontradoException e) {
        return problema(HttpStatus.NOT_FOUND, CodigoErro.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ProblemDetail regraNegocio(RegraNegocioException e) {
        return problema(HttpStatus.UNPROCESSABLE_ENTITY, CodigoErro.BUSINESS_RULE_VIOLATION, e.getMessage());
    }

    @ExceptionHandler(ParametroInvalidoException.class)
    public ProblemDetail parametroInvalido(ParametroInvalidoException e) {
        return problema(HttpStatus.BAD_REQUEST, CodigoErro.INVALID_PARAMETER, e.getMessage());
    }

    /** Proteção extra: ordenação por campo inexistente que escape da validação do service. */
    @ExceptionHandler(PropertyReferenceException.class)
    public ProblemDetail ordenacaoInvalida(PropertyReferenceException e) {
        return problema(HttpStatus.BAD_REQUEST, CodigoErro.INVALID_PARAMETER,
                "Campo de ordenação inválido: " + e.getPropertyName());
    }

    /** Duas operações alteraram a mesma troca ao mesmo tempo (@Version). */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ProblemDetail conflito(ObjectOptimisticLockingFailureException e) {
        return problema(HttpStatus.CONFLICT, CodigoErro.CONFLICT,
                "O recurso foi alterado por outra operação. Consulte novamente e tente de novo.");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail inesperado(Exception e) {
        log.error("Erro inesperado", e);
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, CodigoErro.INTERNAL_ERROR, "Ocorreu um erro inesperado");
    }

    // ---------- Exceções do Spring MVC ----------

    /**
     * Todos os erros do Spring MVC passam por aqui já com o ProblemDetail montado pelo Spring.
     * Só acrescentamos o "code" e traduzimos as mensagens mais comuns.
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception e, Object body, HttpHeaders headers,
                                                             HttpStatusCode status, WebRequest request) {
        ResponseEntity<Object> resposta = super.handleExceptionInternal(e, body, headers, status, request);
        if (resposta == null || !(resposta.getBody() instanceof ProblemDetail problema)) {
            return resposta;
        }

        if (e instanceof MethodArgumentNotValidException invalido) {
            // Campos inválidos no corpo (@Valid) ou nos filtros da query string (ex.: ?status=XYZ)
            problema.setDetail("Requisição inválida");
            problema.setProperty("code", CodigoErro.VALIDATION_ERROR);
            problema.setProperty("errors", errosDeCampo(invalido));
        } else if (e instanceof HttpMessageNotReadableException) {
            problema.setDetail("Corpo da requisição inválido ou malformado");
            problema.setProperty("code", CodigoErro.MALFORMED_REQUEST);
        } else if (e instanceof MethodArgumentTypeMismatchException tipoErrado) {
            problema.setDetail("Valor inválido para o parâmetro '" + tipoErrado.getName() + "': " + tipoErrado.getValue());
            problema.setProperty("code", CodigoErro.INVALID_PARAMETER);
        } else if (e instanceof NoResourceFoundException naoEncontrada) {
            problema.setDetail("Rota não encontrada: /" + naoEncontrada.getResourcePath());
            problema.setProperty("code", CodigoErro.NOT_FOUND);
        } else {
            problema.setProperty("code", CodigoErro.REQUEST_ERROR);
        }
        return resposta;
    }

    private static List<String> errosDeCampo(MethodArgumentNotValidException e) {
        return e.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + (erro.isBindingFailure()
                        ? "valor inválido '" + erro.getRejectedValue() + "'"
                        : erro.getDefaultMessage()))
                .toList();
    }

    private static ProblemDetail problema(HttpStatus status, CodigoErro codigo, String detalhe) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setProperty("code", codigo);
        return problema;
    }
}
