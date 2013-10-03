package com.agileapes.dragonfly.sample.entities;

import javax.persistence.*;
import java.util.Collection;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/10/2, 22:25)
 */
@Entity
@Table(
        name = "authors",
        schema = "test"
)
public class Author {

    private String name;
    private Collection<Book> books;
    private Collection<Book> editedBooks;

    @Column
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ManyToMany(mappedBy = "authors", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    public Collection<Book> getBooks() {
        return books;
    }

    public void setBooks(Collection<Book> books) {
        this.books = books;
    }

    @ManyToMany(mappedBy = "editors", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    public Collection<Book> getEditedBooks() {
        return editedBooks;
    }

    public void setEditedBooks(Collection<Book> editedBooks) {
        this.editedBooks = editedBooks;
    }
}
