import java.io.Serializable;

public class Solicitacao implements Serializable {
    int id;
    String moradorEmail;
    String moradorNome;
    String prestadorEmail;
    String prestadorNome;
    String servico;
    String data;
    String endereco;
    String descricao;
    String status;
    String pagamento;

    public Solicitacao(int id, String moradorEmail, String moradorNome, String servico, String data, String endereco, String descricao) {
        this.id = id;
        this.moradorEmail = moradorEmail;
        this.moradorNome = moradorNome;
        this.prestadorEmail = "";
        this.prestadorNome = "";
        this.servico = servico;
        this.data = data;
        this.endereco = endereco;
        this.descricao = descricao;
        this.status = "aberta";
        this.pagamento = "pendente";
    }

    public String resumo() {
        String prestador = prestadorNome.isBlank() ? "sem prestador" : prestadorNome;
        return "#" + id + " | " + servico + " | " + data + " | " + status + " | Prestador: " + prestador + " | Pagamento: " + pagamento;
    }
}
