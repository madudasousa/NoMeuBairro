package com.meubairro.api.dto.request;


public record EstabRequest(String name, String category, String description, String address,
                           String time, Integer phone, String services, Boolean active, Long createAt) {

}
