package com.example.demo.dtos;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioDTO {

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "America/Sao_Paulo")
    private LocalDateTime geradoEm;
    private SecaoInstituicaoDTO instituicoes;
    private SecaoCuidadorDTO cuidadores;
    private SecaoIdosoDTO idosos;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SecaoInstituicaoDTO {
        private long total;
        private long ativas;
        private long inativas;
        private List<ItemInstituicaoDTO> lista;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SecaoCuidadorDTO {
        private long total;
        private long ativos;
        private long inativos;
        private List<ItemCuidadorDTO> lista;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SecaoIdosoDTO {
        private long total;
        private long ativos;
        private long inativos;
        private List<ItemIdosoDTO> lista;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemInstituicaoDTO {
        private Integer id;
        private String nome;
        private String cnpj;
        private String email;
        private String rua;
        private String bairro;
        private String uf;
        private String status;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemCuidadorDTO {
        private Integer id;
        private String nome;
        private String email;
        private String cpf;
        private String status;
        private String instituicaoNome;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemIdosoDTO {
        private Integer id;
        private String nome;
        private String cpf;
        private String status;
    }

    @Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public static class RelatorioInstituicaoDTO {
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "America/Sao_Paulo")
    private LocalDateTime geradoEm;
    private SecaoCuidadorInstituicaoDTO cuidadores;
    private SecaoIdosoInstituicaoDTO idosos;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public static class SecaoCuidadorInstituicaoDTO {
    private long total;
    private long ativos;
    private long inativos;
    private List<ItemCuidadorInstituicaoDTO> lista;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public static class SecaoIdosoInstituicaoDTO {
    private long total;
    private long ativos;
    private long inativos;
    private List<ItemIdosoInstituicaoDTO> lista;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public static class ItemCuidadorInstituicaoDTO {
    private Integer id;
    private String nome;
    private String email;
    private String cpf;
    private String status;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public static class ItemIdosoInstituicaoDTO {
    private Integer id;
    private String nome;
    private String cpf;
    private String observacoes;
    private String status;
}
}