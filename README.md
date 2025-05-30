# Projeto FindIt - Achados e Perdidos

Este repositório contém o código-fonte do aplicativo móvel *FindIt*, um sistema de achados e perdidos desenvolvido em Java utilizando o Android Studio.

## Índice

* [Pré-requisitos](#pré-requisitos)
* [Instalação](#instalação)
* [Configuração do Firebase](#configuração-do-firebase)
* [Executando o Aplicativo](#executando-o-aplicativo)
* [Testes Funcionais](#testes-funcionais)
* [Contribuição](#contribuição)
* [Licença](#licença)

## Pré-requisitos

Antes de começar, certifique-se de ter instalado em sua máquina:

* [Java JDK 11 ou superior](https://www.oracle.com/java/technologies/javase-downloads.html)
* [Android Studio](https://developer.android.com/studio) (versão recomendada: Arctic Fox ou superior)
* Conexão com a internet para baixar dependências e acessar o Firebase

## Instalação

1. **Clone o repositório**

   ```bash
   git clone https://github.com/KawanWan/findit_mobile.git
   cd findit_mobile
   ```

2. **Abra o projeto no Android Studio**

   * Inicie o Android Studio.
   * Selecione **Open an existing Android Studio project**.
   * Navegue até a pasta do projeto (`findit_mobile`) e confirme.

3. **Sincronize as dependências**

   * Ao abrir o projeto, o Android Studio irá detectar o arquivo `build.gradle` e solicitará sincronização.
   * Clique em **Sync Now** na barra superior ou aguarde a sincronização automática.

## Configuração do Firebase

O aplicativo utiliza o Firebase para autenticação e armazenamento de dados.

1. **Crie um projeto no Firebase Console**

   * Acesse [Firebase Console](https://console.firebase.google.com/).
   * Clique em **Add Project** e siga as instruções.

2. **Adicione um aplicativo Android**

   * No painel do projeto, clique em **Add App** > **Android**.
   * Informe o **Package name** igual ao definido no `AndroidManifest.xml` (ex.: `com.example.finditmobile`).
   * Faça o download do arquivo `google-services.json` fornecido pelo Firebase.

3. **Inclua o arquivo `google-services.json`**

   * Copie o `google-services.json` para a pasta `app/` do seu projeto.

4. **Verifique as dependências**

   No arquivo `app/build.gradle`, certifique-se de ter as seguintes linhas:

   ```groovy
   // Adicione no topo
   apply plugin: 'com.google.gms.google-services'

   dependencies {
       implementation 'com.google.firebase:firebase-auth:21.0.1'
       implementation 'com.google.firebase:firebase-firestore:24.0.0'
       // Outras dependências do Firebase
   }
   ```

   No `build.gradle` do projeto (nível raiz):

   ```groovy
   dependencies {
       classpath 'com.android.tools.build:gradle:7.2.0'
       classpath 'com.google.gms:google-services:4.3.10'
       // ...
   }
   ```

## Executando o Aplicativo

1. **Configure um Emulador ou dispositivo físico**

   * Emulador: Acesse **AVD Manager** e configure um dispositivo Android (API 21+ recomendada).
   * Dispositivo físico: Ative o **USB Debugging** e conecte via USB.

2. **Selecione o dispositivo**

   * No Android Studio, selecione o emulador ou dispositivo conectado na barra de execução.

3. **Execute a aplicação**

   * Clique no botão **Run** (ícone de play) ou pressione **Shift + F10**.

4. **Interaja com o aplicativo**

   * Registre-se ou faça login usando autenticação do Firebase.
   * Teste funcionalidades de registrar item perdido/encontrado e buscar itens.

## Testes Funcionais

Consulte a pasta `docs/` para casos de teste funcionais detalhados. Exemplos:

| ID     | Data       | Cenário                   | Resultado Esperado                 |
| ------ | ---------- | ------------------------- | ---------------------------------- |
| TST001 | 25/05/2025 | Registrar item perdido    | Solicitar detalhes do item         |
| TST002 | 25/05/2025 | Registrar item encontrado | Armazenar informações no Firestore |

## Contribuição

1. Crie um *fork* deste repositório.
2. Crie uma *branch* para sua feature: `git checkout -b feature/nova-funcionalidade`.
3. Faça commit seguindo o Conventional Commits: `git commit -m "feat: adicionar busca por tag"`.
4. Envie para o branch original: `git push origin feature/nova-funcionalidade`.
5. Abra um *Pull Request*.

## Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.
