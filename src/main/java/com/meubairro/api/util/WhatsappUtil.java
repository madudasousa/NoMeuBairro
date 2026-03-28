package com.meubairro.api.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class WhatsappUtil {
    //gera o link direto para conversa no Whatsapp
    public static String gerarLink(String phone){
        if (phone == null || phone.isBlank()) {
            return null;
        }

        //remove tudo que nao for numero
        String numeroLimpo = phone.replaceAll("[^0-9]", "");

        if (!numeroLimpo.startsWith("55")){
            numeroLimpo = "55" + numeroLimpo;
        }

        String mensagem = URLEncoder.encode(
                "Oi! Vim pelo site No Meu Bairro",
                StandardCharsets.UTF_8
        );

        return "https://wa.me/" + numeroLimpo;
    }
}
