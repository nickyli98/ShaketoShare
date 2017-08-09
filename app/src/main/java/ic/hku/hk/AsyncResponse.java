package ic.hku.hk;

public interface AsyncResponse{
    <T> void processFinish(T output);
}