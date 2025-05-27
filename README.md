# PeopleList - App Android

Um aplicativo Android robusto que consome a API pública de usuários do JSONPlaceholder, implementado com Clean Architecture, MVVM e seguindo as melhores práticas de desenvolvimento Android.
Tempo gasto de 12 horas (3 dias)

## Sobre o App

O PeopleList permite visualizar uma lista de usuários com funcionalidades de busca, paginação e detalhamento. O app funciona tanto online quanto offline, utilizando cache local para uma melhor experiência do usuário.

**API Base:** https://jsonplaceholder.typicode.com/users

## Funcionalidades Implementadas

### Obrigatórias
- [x] **Tela de listagem** com nome, e-mail e cidade do usuário
- [x] **Lista paginada** (simulada com pageSize = 3)
- [x] **Pull-to-refresh** para atualizar dados
- [x] **Tela de detalhes** com todas as informações do usuário
- [x] **Campo de busca** por nome e e-mail
- [x] **Persistência local** com Room Database
- [x] **Tratamento de erros** de rede e parsing

### Diferenciais Técnicos
- [x] **Testes Unitários e de UI** (MockK + Espresso)
- [x] **Clean Architecture** (Presentation → Domain → Data)
- [x] **Injeção de dependência** com Koin
- [x] **Modularização** (app, core, network, database)
- [ ] Jetpack Compose (implementado com View System)
- [ ] CI/CD

## Arquitetura

### Clean Architecture - 3 Camadas

```
┌─────────────────────────────────────────┐
│            PRESENTATION                 │
│  ┌─────────────┐    ┌─────────────┐    │
│  │  Fragment   │    │  ViewModel  │    │
│  └─────────────┘    └─────────────┘    │
└─────────────────────────────────────────┘
                       │
┌─────────────────────────────────────────┐
│              DOMAIN                     │
│  ┌─────────────┐    ┌─────────────┐    │
│  │  Use Cases  │    │ Repository  │    │
│  │             │    │ (Interface) │    │
│  └─────────────┘    └─────────────┘    │
└─────────────────────────────────────────┘
                       │
┌─────────────────────────────────────────┐
│               DATA                      │
│  ┌─────────────┐    ┌─────────────┐    │
│  │ Repository  │    │ DataSource  │    │
│  │    Impl     │    │(Remote/Local)│   │
│  └─────────────┘    └─────────────┘    │
└─────────────────────────────────────────┘
```

### Fluxo de Dados
```
Fragment → ViewModel → UseCase → Repository → DataSource → API/Database
```

## Estrutura de Módulos

```
project/
├── app/                          # Módulo principal da aplicação
│   ├── presentation/             # Activities, Fragments, ViewModels
│   ├── data/                     # Repository implementations, PagingSource
│   └── di/                       # Configuração Koin
├── core/                         # Entidades de domínio e utilitários
│   ├── User.kt                   # Entidade principal
│   └── Utils.kt                  # Verificação de conectividade
├── network/                      # Comunicação com API
│   ├── service/                  # Retrofit interfaces
│   ├── dto/                      # Data Transfer Objects
│   ├── datasource/               # Remote DataSource
│   └── extensions/               # ApiResult, NetworkHandler
└── database/                     # Persistência local
    ├── dao/                      # Room DAOs
    ├── entity/                   # Entidades do Room
    └── datasource/               # Local DataSource
```

## Tecnologias Utilizadas

### Core
- **Kotlin** - Linguagem principal
- **Android SDK 24+** - Suporte mínimo
- **View System** - Interface do usuário

### Arquitetura & Padrões
- **Clean Architecture** - Separação de responsabilidades
- **MVVM** - Padrão de apresentação
- **Repository Pattern** - Abstração de dados
- **Single Source of Truth** - Centralizando fontes de dados

### Networking
- **Retrofit** - Cliente HTTP
- **OkHttp** - Cliente HTTP base
- **Gson** - Serialização JSON

### Database
- **Room** - Persistência local
- **SQLite** - Database engine

### Dependency Injection
- **Koin** - Injeção de dependência

### Async & Threading
- **Kotlin Coroutines** - Programação assíncrona
- **Flow** - Streams reativos

### Pagination
- **Paging 3** - Paginação eficiente

### UI & Navigation
- **View Binding** - Binding de views
- **Navigation Component** - Navegação entre telas
- **Material Design** - Componentes de UI
- **SwipeRefreshLayout** - Pull-to-refresh

### Testing
- **JUnit 4** - Framework de testes
- **MockK** - Mocking para Kotlin
- **Espresso** - Testes de UI
- **Fragment Testing** - Testes de fragmentos
- **Coroutines Test** - Testes de corrotinas

## Funcionalidades Avançadas

### Sistema de Cache Inteligente
- **Cache Remoto:** Dados em memória no RemoteDataSource
- **Cache Local:** Persistência com Room Database
- **Fallback Automático:** Usa cache quando offline
- **Notificações:** Toast informativo sobre uso de cache

### Tratamento de Conectividade
- **Verificação de Rede:** Detecta conexão automaticamente
- **Modo Offline:** Funciona sem internet usando cache
- **Sincronização:** Atualiza dados quando conexão volta

### Estados de Carregamento
- **Loading States:** Indicadores visuais de carregamento
- **Error Handling:** Tratamento robusto de erros
- **Retry Mechanism:** Tentativas automáticas e manuais

## Testes Implementados

### Testes Unitários
- **UserDetailViewModelTest** - Testa lógica do ViewModel
  - Carregamento de dados
  - Tratamento de erros
  - Uso de cache
  - Funcionalidade retry

### Testes de UI
- **UserListFragmentUITest** - Testa interface do usuário
  - Exibição de lista
  - Funcionalidade de busca
  - Pull-to-refresh
  - Navegação
  - Mensagens de erro/cache

## Como Executar

### Pré-requisitos
- Android Studio Arctic Fox ou superior
- JDK 11
- Android SDK 24+

### Configuração
1. Clone o repositório
```bash
git clone https://github.com/caiocesar-gf/people-list
cd PeopleList
```

2. Abra no Android Studio

3. Sincronize as dependências Gradle

4. Execute o app
```bash
./gradlew assembleDebug
```

### Executar Testes
```bash
# Testes unitários
./gradlew test


# Todos os testes
./gradlew check
```

## Permissões

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## Decisões Técnicas

### Por que View System ao invés de Compose?
- **Estabilidade:** View System é mais maduro e estável
- **Compatibilidade:** Melhor suporte para APIs antigas
- **Paging 3:** Integração mais robusta com PagingDataAdapter

### Por que Koin ao invés de Hilt?
- **Simplicidade:** Configuração mais simples
- **Kotlin First:** Projetado especificamente para Kotlin
- **Menos Boilerplate:** Menos código de configuração

### Estratégia de Cache
- **Duplo Cache:** Remoto (rápido) + Local (persistente)
- **Cache-First:** Sempre tenta usar cache quando offline
- **Smart Sync:** Sincroniza apenas quando há mudanças

## Próximos Passos

- [ ] Migração para Jetpack Compose
- [ ] Implementação de CI/CD
- [ ] Testes de integração
- [ ] Offline-first architecture
- [ ] Push notifications
- [ ] Dark theme

## Licença

Este projeto foi desenvolvido como parte de um desafio técnico.
