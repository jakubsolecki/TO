import io.reactivex.rxjava3.core.Single;

public class Main  {

    public static void main(String[] args) {
        var hello = Single.just("Hello TO!");
		hello.subscribe(System.out::println);
    }
}