package br.gov.df.seagri.dominio_central.infraestrutura;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

import jakarta.annotation.PostConstruct;

@Service
@Slf4j
public class ArmazenamentoLocalSrv {

    @Value("${armazenamento.diretorio.fotos:#{systemProperties['user.home'] + '/seagri_fotos'}}")
    private String diretorioRaiz;

    private Path getDiretorioRaiz() {
        return this.diretorioRaiz == null ? null : Paths.get(this.diretorioRaiz);
    }

    @PostConstruct
    public void init() {
        System.out.println(diretorioRaiz);
        try {
            log.debug("diretorioRaiz: [{}]", diretorioRaiz);
            if (!Files.exists(getDiretorioRaiz())) {
                log.trace("Criando o diretório raiz das fotos em {}", diretorioRaiz);
                Files.createDirectories(getDiretorioRaiz());
            }
        } catch (IOException e) {
            log.error("❌ Erro fatal ao criar diretório de biometria: {}", e.getMessage());
        }
    }

    public UUID salvarFotoBase64(String fotoBase64) {
        if (fotoBase64 == null || fotoBase64.isEmpty()) {
            log.debug("fotoBase64 vazio ou não informado");
            return null;
        }

        try {
            // 1. Isola apenas a string Base64 pura (Remove o prefixo 'data:image/jpeg;base64,')
            String base64Limpo = fotoBase64;
            if (fotoBase64.contains(",")) {
                base64Limpo = fotoBase64.substring(fotoBase64.indexOf(",") + 1);
            }

            // 2. Limpeza profunda: Remove espaços e quebras de linha que o JSON pode ter injetado
            base64Limpo = base64Limpo.replaceAll("\\s+", "");

            // 3. Decodifica a imagem binária
            byte[] bytesImagem = Base64.getDecoder().decode(base64Limpo);

            // 4. Salva no disco (Garante a imutabilidade gerando um UUID único para o arquivo)
            UUID referenciaImagem = UUID.randomUUID();
            Path caminhoArquivo = getDiretorioRaiz().resolve(referenciaImagem.toString() + ".jpg");

            Files.write(caminhoArquivo, bytesImagem);
            
            log.debug("💾 FOTO SALVA COM SUCESSO NO DISCO: {}", caminhoArquivo.toString());

            return referenciaImagem;

        } catch (IllegalArgumentException e) {
            log.error("❌ ERRO DE DECODIFICAÇÃO BASE64: A imagem não veio no formato correto.");
            e.printStackTrace();
            throw new RuntimeException("Falha de formato da imagem", e);
        } catch (IOException e) {
            log.error("❌ ERRO DE GRAVAÇÃO NO DISCO: O Java não tem permissão para salvar na pasta.");
            e.printStackTrace();
            throw new RuntimeException("Falha de gravação de arquivo", e);
        } catch (Exception e) {
            log.error("❌ ERRO DESCONHECIDO NO ARMAZENAMENTO.");
            e.printStackTrace();
            throw new RuntimeException("Erro interno no armazenamento", e);
        }
    }

    // Método para ler a imagem física e devolvê-la para a web
    public byte[] recuperarFoto(UUID referenciaImagem) {
        if (referenciaImagem == null) {
            log.debug("ID de Referência da imagem não informado");
            return null;
        }

        try {
            Path caminhoArquivo = getDiretorioRaiz().resolve(referenciaImagem.toString() + ".jpg");
            if (Files.exists(caminhoArquivo)) {
                byte[] result = Files.readAllBytes(caminhoArquivo);
                log.debug("ID de Referência da imagem {} encontrado, tamanho {}.", referenciaImagem.toString(), result.length);
                return result;
            }
            log.debug("ID de Referência ({}) não encontrado no disco", referenciaImagem.toString());
            return null;
        } catch (IOException e) {
            log.error("❌ Erro ao ler imagem do disco: " + e.getMessage());
            throw new RuntimeException("Falha ao ler evidência biométrica", e);
        }
    }

}