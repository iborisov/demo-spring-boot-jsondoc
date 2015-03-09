package ru.iborisov.jsondocdemo;

import org.jsondoc.core.annotation.*;
import org.jsondoc.spring.boot.starter.EnableJSONDoc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@SpringBootApplication
@EnableJSONDoc
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    static class Book {
        public final Long id;
        public final String title;

        public Book(Long id, String title) {
            this.id = id;
            this.title = title;
        }
    }

    @Api(name = "books services", description = "Methods for managing books list")
    @RestController
    @RequestMapping("/books")
    static class BooksController {
        // Dumb way to store entities list.
        private final AtomicLong nextBookId = new AtomicLong(0);
        private final ConcurrentMap<Long, Book> books = new ConcurrentHashMap<>();

        @ApiMethod(
                description = "Get list of all books",
                produces = {MediaType.APPLICATION_JSON_VALUE})
        @ApiResponseObject
        @RequestMapping(value = "/", method = RequestMethod.GET)
        public Collection<Book> getAllBooks() {
            List<Book> allBooks = new ArrayList<>();
            for (Map.Entry<Long, Book> booksMapEntry : books.entrySet()) {
                allBooks.add(new Book(booksMapEntry.getKey(), booksMapEntry.getValue().title));
            }
            return allBooks;
        }

        @ApiMethod(
                description = "Add new book to list",
                produces = {MediaType.APPLICATION_JSON_VALUE})
        @ApiResponseObject
        @RequestMapping(value = "/", method = RequestMethod.POST)
        public ResponseEntity<?> addBook(
                @ApiQueryParam(name = "title", description = "title for book to add")
                @RequestParam(value = "title") String title,
                UriComponentsBuilder uriBuilder) {
            long bookId = nextBookId.getAndIncrement();
            books.put(bookId, new Book(bookId, title));
            URI uri = uriBuilder.path("/books/{bookId}")
                    .buildAndExpand(bookId).toUri();
            return ResponseEntity.created(uri).build();
        }

        @ApiMethod(
                description = "Get book by ID",
                produces = {MediaType.APPLICATION_JSON_VALUE})
        @ApiResponseObject
        @RequestMapping(value = "/{bookId}", method = RequestMethod.GET)
        public Book getBook(
                @ApiPathParam(name = "bookId", description = "Book ID")
                @PathVariable(value = "bookId") Long bookId) {
            return books.get(bookId);
        }
    }
}
