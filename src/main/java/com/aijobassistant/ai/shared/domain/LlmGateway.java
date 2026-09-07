package com.aijobassistant.ai.shared.domain;

public interface LlmGateway {

    /**
     * Envía una consulta al modelo requiriendo una respuesta en formato JSON estructurado.
     *
     * @param systemPrompt Directivas del sistema, rol y restricciones de respuesta.
     * @param userPrompt   Contenido o documentos que el modelo debe procesar.
     * @return Cadena JSON devuelta por el modelo.
     * @throws com.aijobassistant.ai.shared.exception.LlmServiceException si ocurre un error de comunicación o respuesta inválida.
     */
    String generateStructuredResponse(String systemPrompt, String userPrompt);
}