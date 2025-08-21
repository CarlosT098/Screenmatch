package br.com.alura.screenmatch.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

public class ConsultaChatGPT {

  public static String obterTraducao(String texto)  {
    Client client = Client.builder()
    .apiKey(System.getenv("GEMINI_APIKEY"))
    .build();

    GenerateContentResponse response =
        client.models.generateContent(
            "gemini-2.5-flash",
            "apenas faça traducao para o português o texto: " + texto,
            null);
    return response.text();
  }
}
