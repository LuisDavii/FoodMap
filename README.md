Com certeza. Aqui está uma proposta completa e profissional de `README.md` para o seu projeto **FoodMap**, estruturada para portfólio e documentação técnica, sem emojis conforme solicitado.

Você pode copiar e colar este conteúdo diretamente no arquivo `README.md` do seu repositório.

-----

# FoodMap - Gestão Nutricional Inteligente com Visão Computacional

O **FoodMap** é uma aplicação móvel nativa para Android desenvolvida para facilitar o controle e monitoramento nutricional. O projeto integra uma arquitetura cliente-servidor robusta com recursos de Inteligência Artificial *on-device* para identificação automática de alimentos através da câmera do dispositivo.

## Visão Geral do Projeto

O objetivo principal do FoodMap é simplificar o processo de registro de refeições diárias. Diferente de aplicativos tradicionais onde a entrada de dados é puramente manual, o FoodMap utiliza um modelo de Machine Learning customizado para classificar alimentos em tempo real, agilizando o preenchimento de diários alimentares e o cálculo de metas calóricas.

## Arquitetura da Solução

O sistema foi desenvolvido utilizando uma arquitetura distribuída, garantindo escalabilidade e separação de responsabilidades.

### Cliente (Dispositivo Móvel)

  * **Plataforma:** Android Nativo.
  * **Linguagem:** Kotlin.
  * **Interface:** XML com ConstraintLayout e Material Design.
  * **Comunicação:** Retrofit para consumo de API REST.
  * **Persistência Local:** SharedPreferences para gerenciamento de sessão e tokens de autenticação.
  * **Multimídia:** CameraX para captura e processamento de imagens em tempo real.

### Servidor (Backend)

  * **Linguagem:** Python.
  * **Framework:** Django e Django REST Framework (DRF).
  * **Banco de Dados:** PostgreSQL (Relacional).
  * **Hospedagem:** Localhost (ambiente de desenvolvimento) configurado para acesso via rede local.

### Inteligência Artificial (Visão Computacional)

  * **Framework:** TensorFlow Lite.
  * **Modelo Base:** MobileNetV2 (otimizado para dispositivos móveis).
  * **Técnica de Treinamento:** Transfer Learning (Aprendizagem por Transferência).
  * **Processamento de Imagem:** Pré-processamento customizado no Android (Corte central, Redimensionamento e Normalização de pixels) para garantir alta acurácia na inferência.

## Funcionalidades Principais

1.  **Autenticação e Gestão de Sessão:**

      * Sistema de cadastro e login seguro.
      * Persistência de login automática (o usuário permanece logado ao reiniciar o app).
      * Funcionalidade de logout seguro.

2.  **Scanner de Alimentos (IA):**

      * Detecção de alimentos em tempo real utilizando a câmera.
      * Feedback visual com moldura de captura para garantir enquadramento correto.
      * Filtro de confiança: o sistema apenas sugere resultados se a acurácia do modelo superar um limiar predefinido (ex: 70%).

3.  **Planejamento Semanal:**

      * Visualização das refeições organizadas por dia da semana.
      * Sincronização de dados com o servidor em nuvem.
      * Marcação de refeições como "Concluídas" com atualização instantânea no banco de dados.

4.  **Estatísticas e Metas:**

      * Definição personalizada de metas calóricas diárias.
      * Gráficos de progresso diário e semanal.
      * Cálculo automático de calorias ingeridas baseado apenas nas refeições marcadas como concluídas.

## Tecnologias e Bibliotecas Utilizadas

### Android (build.gradle)

  * **Core:** AndroidX, AppCompat, Material Design.
  * **Rede:** Retrofit 2, Gson Converter.
  * **Câmera:** CameraX (Core, Camera2, Lifecycle, View).
  * **Machine Learning:** TensorFlow Lite, TensorFlow Lite Support, TensorFlow Lite GPU.

### Backend (requirements.txt)

  * Django
  * djangorestframework
  * psycopg2-binary (Conector PostgreSQL)
  * django-cors-headers

## Configuração e Instalação

### Pré-requisitos

  * Android Studio Iguana ou superior.
  * Python 3.10 ou superior.
  * PostgreSQL instalado e rodando.
  * Dispositivo Android físico para testes de câmera (Emuladores podem não suportar aceleração de hardware para o TensorFlow Lite corretamente).

### Passo a Passo

1.  **Configuração do Banco de Dados:**

      * Crie um banco de dados no PostgreSQL chamado `foodmap_db`.
      * Atualize as credenciais no arquivo `settings.py` do Django.

2.  **Execução do Backend:**

    ```bash
    python manage.py makemigrations
    python manage.py migrate
    python manage.py runserver 0.0.0.0:8000
    ```

3.  **Configuração do Android:**

      * Abra o projeto no Android Studio.
      * Navegue até `network/RetrofitClient.kt`.
      * Atualize a constante `BASE_URL` com o endereço IP da sua máquina na rede local (ex: `http://192.168.1.X:8000/`).
      * Certifique-se de que o dispositivo móvel e o computador estejam na mesma rede Wi-Fi.

4.  **Compilação:**

      * Realize o Sync do Gradle.
      * Execute o aplicativo no dispositivo conectado.

## Estrutura de Pastas (Android)

  * `models/`: Classes de dados (Data Classes) para mapeamento de JSON.
  * `network/`: Configuração do Retrofit e interfaces de API.
  * `ui/`: Activities e Adapters (Lógica de apresentação).
  * `assets/`: Arquivos do modelo TensorFlow Lite (`.tflite`) e rótulos (`labels.txt`).

-----

**Desenvolvido como parte de projeto acadêmico de desenvolvimento mobile e sistemas distribuídos.**
