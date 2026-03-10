package br.gov.df.seagri.dominio_central.infraestrutura;

import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

@Service
public class ArmazenamentoLocalSrv {

    private final Path diretorioRaiz = Paths.get(System.getProperty("user.home"), "seagri_biometria");

    public ArmazenamentoLocalSrv() {
        try {
            if (!Files.exists(diretorioRaiz)) {
                Files.createDirectories(diretorioRaiz);
            }
        } catch (IOException e) {
            System.err.println("❌ Erro fatal ao criar diretório de biometria: " + e.getMessage());
        }
    }

    public UUID salvarFotoBase64(String fotoBase64) {
        if (fotoBase64 == null || fotoBase64.isEmpty()) {
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
            Path caminhoArquivo = diretorioRaiz.resolve(referenciaImagem.toString() + ".jpg");

            Files.write(caminhoArquivo, bytesImagem);
            
            System.out.println("💾 FOTO SALVA COM SUCESSO NO DISCO: " + caminhoArquivo.toString());

            return referenciaImagem;

        } catch (IllegalArgumentException e) {
            System.err.println("❌ ERRO DE DECODIFICAÇÃO BASE64: A imagem não veio no formato correto.");
            e.printStackTrace();
            throw new RuntimeException("Falha de formato da imagem", e);
        } catch (IOException e) {
            System.err.println("❌ ERRO DE GRAVAÇÃO NO DISCO: O Java não tem permissão para salvar na pasta.");
            e.printStackTrace();
            throw new RuntimeException("Falha de gravação de arquivo", e);
        } catch (Exception e) {
            System.err.println("❌ ERRO DESCONHECIDO NO ARMAZENAMENTO.");
            e.printStackTrace();
            throw new RuntimeException("Erro interno no armazenamento", e);
        }
    }

    // Método para ler a imagem física e devolvê-la para a web
    public byte[] recuperarFoto(UUID referenciaImagem) {
        if (referenciaImagem == null) return null;
        try {
            Path caminhoArquivo = diretorioRaiz.resolve(referenciaImagem.toString() + ".jpg");
            if (java.nio.file.Files.exists(caminhoArquivo)) {
                return java.nio.file.Files.readAllBytes(caminhoArquivo);
            }
            return null;
        } catch (IOException e) {
            System.err.println("❌ Erro ao ler imagem do disco: " + e.getMessage());
            throw new RuntimeException("Falha ao ler evidência biométrica", e);
        }
    }
}