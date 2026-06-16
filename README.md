# Zlar Oficial - Sistema Web em Java

Este projeto e uma versao simples do sistema Zlar feita em **Java puro**.

Ele roda no navegador, mas nao usa Spring Boot, Maven, PHP, JavaScript ou banco de dados externo.
O objetivo foi deixar o codigo facil de entender, alterar e apresentar.

## Como executar

Abra a pasta do projeto no IntelliJ ou no terminal:

```text
C:\Users\User\OneDrive\Documentos\zlar_oficial
```

Para rodar pelo terminal, execute:

```bat
rodar.bat
```

Depois abra no navegador:

```text
http://localhost:8090
```

Se a porta 8090 estiver ocupada, rode com outra porta:

```bat
java -DPORT=8091 -cp out\production\zlar_oficial ZlarApp
```

E acesse:

```text
http://localhost:8091
```

## Login do administrador

```text
Usuario: zlar2026
Codigo: 747171
```

## O que o sistema faz

O sistema possui tres tipos de usuario:

- Morador
- Prestador
- Administrador

O morador consegue:

- Criar uma conta
- Fazer login
- Abrir uma solicitacao de servico
- Ver se algum prestador aceitou
- Ver se o servico foi concluido
- Pagar uma solicitacao concluida
- Abrir chamado de suporte

O prestador consegue:

- Criar uma conta
- Fazer login
- Ver solicitacoes do servico que ele oferece
- Aceitar solicitacoes
- Concluir solicitacoes aceitas
- Abrir chamado de suporte

O administrador consegue:

- Fazer login
- Ver usuarios cadastrados
- Ver chamados de suporte
- Responder chamados de suporte

## Fluxo principal

1. O morador cria uma solicitacao de servico.
2. O prestador entra no painel dele.
3. O prestador ve as solicitacoes relacionadas ao servico dele.
4. O prestador clica em `Aceitar`.
5. O morador passa a ver o nome do prestador na solicitacao.
6. O prestador clica em `Concluir`.
7. O morador clica em `Pagar`.
8. A solicitacao fica marcada como paga.

## Estrutura do projeto

```text
src/
  ZlarApp.java          Arquivo que inicia o sistema
  ServidorWeb.java      Controla as rotas e botoes do site
  Pagina.java           Carrega os arquivos HTML da pasta templates
  BancoDados.java       Guarda os dados em listas na memoria
  Usuario.java          Classe que representa um usuario
  Solicitacao.java      Classe que representa uma solicitacao
  Chamado.java          Classe que representa um chamado de suporte

templates/
  Arquivos HTML das telas do sistema

web/
  estilo.css            Arquivo de aparencia do sistema

compilar.bat            Compila os arquivos Java
rodar.bat               Compila e inicia o sistema
```

## Como o projeto funciona

O navegador acessa uma rota, por exemplo:

```text
/login
```

O Java recebe essa rota no arquivo:

```text
ServidorWeb.java
```

Depois o Java carrega um arquivo HTML da pasta:

```text
templates
```

Quando o usuario envia um formulario, o Java recebe os dados, executa a regra e devolve uma nova pagina pronta.

## Sobre os dados

Este projeto guarda os dados em memoria usando listas Java.

Exemplo:

```java
static List<Usuario> usuarios = new ArrayList<>();
static List<Solicitacao> solicitacoes = new ArrayList<>();
static List<Chamado> chamados = new ArrayList<>();
```

Isso significa que os cadastros existem enquanto o sistema estiver aberto.
Se fechar o programa e abrir de novo, os dados voltam ao inicio.

Essa escolha foi feita para deixar o projeto mais simples de explicar.

## Como adicionar um campo novo no cadastro

Exemplo: adicionar o campo `nomeMae`.

No arquivo:

```text
templates/cadastro.html
```

adicione dentro do formulario:

```html
<div class="field">
  <label>Nome da mae</label>
  <input name="nomeMae">
</div>
```

Depois, no Java, esse campo pode ser lido assim:

```java
form.get("nomeMae")
```

Se quiser salvar esse campo no usuario, tambem e necessario adicionar esse atributo na classe `Usuario`.

## Como explicar para o professor

Uma explicacao simples:

> O projeto foi feito em Java puro. O arquivo `ZlarApp` inicia o servidor. O arquivo `ServidorWeb` recebe as rotas do navegador. Os arquivos HTML ficam separados na pasta `templates`. Os dados ficam em listas dentro da classe `BancoDados`, para o projeto ficar simples e facil de apresentar.

Outra explicacao:

> Quando o usuario clica em um botao, o navegador envia um formulario para o Java. O Java processa os dados, atualiza as listas e devolve a tela atualizada.

## Observacoes

- O projeto nao usa JavaScript.
- O projeto nao usa Spring Boot.
- O projeto nao usa banco de dados externo.
- O projeto foi pensado para ser simples e facil de alterar.
- A pasta `out` e gerada automaticamente na compilacao.

