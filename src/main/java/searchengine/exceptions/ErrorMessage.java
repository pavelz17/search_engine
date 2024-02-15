package searchengine.exceptions;

public enum ErrorMessage {
    SITE_OUT_OF_BOUND_CONFIG_FILE("Данная страница находится за пределами сайтов, указанных в конфигурационном файле"),
    CONNECT_ERROR("Не удалось подключиться к странице: "),
    INTERRUPT_INDEXING("Индексация остановлена пользователем"),
    START_INDEXING("Индексация уже запущена"),
    STOP_INDEXING("Индексация не запущена"),
    SEARCH_STATUS("Сайт не проиндексирован"),
    QUERY_IS_EMPTY("Задан пустой поисковый запрос");

    private final String msg;

    ErrorMessage(String msg) {
        this.msg = msg;
    }

    public String getMessage() {
        return msg;
    }
}
