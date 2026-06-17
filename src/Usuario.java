import java.io.Serializable;

public class Usuario implements Serializable {
    int id;
    String tipo;
    String nome;
    String email;
    String telefone;
    String senha;
    String servicoOuEndereco;

    public Usuario(int id, String tipo, String nome, String email, String telefone, String senha, String servicoOuEndereco) {
        this.id = id;
        this.tipo = tipo;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.senha = senha;
        this.servicoOuEndereco = servicoOuEndereco;
    }

    public String resumo() {
        return id + " - " + tipo + " - " + nome + " - " + email;
    }
}
