package searchengine.mappers;

public interface Mapper<T, E> {
    E mapFrom(T obj);
}
