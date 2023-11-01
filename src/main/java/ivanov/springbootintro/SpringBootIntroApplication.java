package ivanov.springbootintro;

import ivanov.springbootintro.model.Book;
import ivanov.springbootintro.service.BookService;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringBootIntroApplication {
    @Autowired
    private BookService bookService;

    public static void main(String[] args) {
        SpringApplication.run(SpringBootIntroApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            Book book = new Book();
            book.setTitle("Kobzar");
            book.setAuthor("Taras Shevchenko");
            book.setIsbn("unknown");
            book.setPrice(BigDecimal.valueOf(352.00));
            book.setDescription("Lyric");
            book.setCoverImage("Kobza");
            bookService.save(book);
            System.out.println(bookService.findAll());
        };
    }

}
