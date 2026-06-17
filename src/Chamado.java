import java.io.Serializable;

public class Chamado implements Serializable {
    int id;
    String usuarioEmail;
    String usuarioNome;
    String assunto;
    String descricao;
    String status;
    String resposta;

    public Chamado(int id, String usuarioEmail, String usuarioNome, String assunto, String descricao) {
        this.id = id;
        this.usuarioEmail = usuarioEmail;
        this.usuarioNome = usuarioNome;
        this.assunto = assunto;
        this.descricao = descricao;
        this.status = "aberto";
        this.resposta = "";
    }

    public String resumo() {
        String textoResposta = resposta.isBlank() ? "sem resposta" : resposta;
        return "#" + id + " | " + usuarioNome + " | " + assunto + " | " + status + " | " + textoResposta;
    }
}
