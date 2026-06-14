package com.example.demo.utils;

import java.text.Normalizer;
import java.util.Locale;

public final class TextoUtils {

    private TextoUtils() {
    }

    public static String paraBanco(String valor) {
        if (valor == null) {
            return null;
        }

        String textoSemAcentos = removerAcentos(valor.trim());
        return textoSemAcentos.toUpperCase(Locale.ROOT);
    }

    public static String paraExibicao(String valor) {
        if (valor == null) {
            return null;
        }

        String texto = valor.trim().toLowerCase(Locale.ROOT);
        if (texto.isEmpty()) {
            return texto;
        }

        String[] palavras = texto.split("\\s+");
        StringBuilder resultado = new StringBuilder();

        for (String palavra : palavras) {
            if (resultado.length() > 0) {
                resultado.append(" ");
            }

            resultado.append(Character.toUpperCase(palavra.charAt(0)));
            if (palavra.length() > 1) {
                resultado.append(palavra.substring(1));
            }
        }

        return resultado.toString();
    }

    public static String textoLivre(String valor) {
        return valor;
    }

    public static String limparDocumento(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.replaceAll("\\D", "");
    }

    public static String limparNumero(String valor) {
        return limparDocumento(valor);
    }

    private static String removerAcentos(String valor) {
        String textoNormalizado = Normalizer.normalize(valor, Normalizer.Form.NFD);
        return textoNormalizado.replaceAll("\\p{M}", "");
    }

    public static String paraUf(String valor) {
        if (valor == null) {
            return null;
        }
        return valor.trim().toUpperCase(Locale.ROOT);
    }
}
