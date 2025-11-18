package com.dianaglobal.paineldoauthorbackend.adapter.in.dto;

import jakarta.validation.constraints.NotBlank;

public record GenericTokenDTO(@NotBlank String token) {}
