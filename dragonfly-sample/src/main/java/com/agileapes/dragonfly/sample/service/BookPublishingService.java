package com.agileapes.dragonfly.sample.service;

import com.agileapes.dragonfly.data.DataAccess;
import com.agileapes.dragonfly.metadata.MetadataRegistry;
import com.agileapes.dragonfly.metadata.ReferenceMetadata;
import com.agileapes.dragonfly.metadata.TableMetadata;
import com.agileapes.dragonfly.sample.entities.Author;
import com.agileapes.dragonfly.sample.entities.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/10/2, 22:36)
 */
@Service
public class BookPublishingService {

    @Autowired
    private DataAccess dataAccess;

    @Autowired
    private MetadataRegistry metadataRegistry;

    public void execute() {
        final TableMetadata<Book> tableMetadata = metadataRegistry.getTableMetadata(Book.class);
        final Collection<ReferenceMetadata<Book,?>> foreignReferences = tableMetadata.getForeignReferences();
        for (ReferenceMetadata<Book, ?> foreignReference : foreignReferences) {
            System.out.println(foreignReference);
        }
        final Book bookA = getBook("Book A");
        final Book bookB = getBook("Book B");
        final Author authorA = getAuthor("Author A");
        final Author authorB = getAuthor("Author B");
        final Author authorC = getAuthor("Author C");
        bookA.setAuthors(Arrays.asList(authorA, authorB));
        bookA.setEditors(Arrays.asList(authorB, authorC));
        bookB.setAuthors(Arrays.asList(authorB, authorC));
        bookB.setEditors(Arrays.asList(authorA, authorC));
        dataAccess.save(bookA);
        dataAccess.save(bookB);
        dataAccess.find(authorB);
        for (Book book : authorB.getBooks()) {
            System.out.println(book.getTitle());
        }
        for (Book book : authorB.getEditedBooks()) {
            System.out.println(book.getTitle());
        }
    }

    private Book getBook(String title) {
        final Book book = new Book();
        book.setTitle(title);
        return book;
    }

    private Author getAuthor(String name) {
        final Author author = new Author();
        author.setName(name);
        return author;
    }

}
