package com.nearsoft.training.library.service.impl;

import com.nearsoft.training.library.config.LoanConfigurationProperties;
import com.nearsoft.training.library.exception.LoanNotAllowedException;
import com.nearsoft.training.library.model.Book;
import com.nearsoft.training.library.model.BooksByUser;
import com.nearsoft.training.library.model.User;
import com.nearsoft.training.library.repository.BookRepository;
import com.nearsoft.training.library.service.CardReaderService;
import com.nearsoft.training.library.service.UserService;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class LoanServiceImplTest {

    @Test
    public void GivenAUserWithoutLoans_WhenLendBooks_ThenTheBookIsLended(){

        //Given:
        CardReaderService cardReaderService = Mockito.mock(CardReaderService.class);
        UserService userService = Mockito.mock(UserService.class);
        LoanConfigurationProperties loanConfigurationProperties = Mockito.mock(LoanConfigurationProperties.class);
        BookRepository bookRepository = Mockito.mock(BookRepository.class);
        LoanServiceImpl loanService = new LoanServiceImpl(cardReaderService,userService,loanConfigurationProperties,bookRepository);

        Set<BooksByUser> booksByUser = new HashSet<>();
        User user = new User();
        Book book = new Book();
        String curp = "CURP";
        String []isbnList = {"SJ926","PF364"};
        user.setCurp(curp);

        Mockito.when(cardReaderService.readUser()).thenReturn(user);
        Mockito.when(userService.getBorrowedBooks(curp)).thenReturn(booksByUser);
        Mockito.when(loanConfigurationProperties.getMaxBooksPerUser()).thenReturn(15);
        Mockito.when(bookRepository.findById("SJ926")).thenReturn(Optional.of(book));
        Mockito.when(bookRepository.findById("PF364")).thenReturn(Optional.empty());

        ArgumentCaptor<Book> argumentCaptor = ArgumentCaptor.forClass(Book.class);

        //When:
        loanService.lendBooks(isbnList);

        //Then:
        Mockito.verify(bookRepository).findById("SJ926");
        Mockito.verify(bookRepository).findById("PF364");
        Mockito.verify(bookRepository).save(argumentCaptor.capture());

        Mockito.verify(userService).registerLoan(user,isbnList);

        Book registredBook = argumentCaptor.getValue();

        assertEquals("PF364",registredBook.getIsbn());
    }

    @Test
    public void GivenAnUserWithLoans_WhenReturnBooks_ThenTheReturnIsRegistred() {
        //Given:
        CardReaderService cardReaderService = Mockito.mock(CardReaderService.class);
        UserService userService = Mockito.mock(UserService.class);
        LoanConfigurationProperties loanConfigurationProperties = Mockito.mock(LoanConfigurationProperties.class);
        BookRepository bookRepository = Mockito.mock(BookRepository.class);
        LoanServiceImpl loanService = new LoanServiceImpl(cardReaderService,userService,loanConfigurationProperties,bookRepository);

        String[] isbnList = {"SB129","FO385"};
        User user = new User();

        Mockito.when(cardReaderService.readUser()).thenReturn(user);

        //When:
        loanService.returnBooks(isbnList);

        //Then:
        Mockito.verify(userService).registerReturn(user,isbnList);
    }

    @Test
    public void GivenAnUserWithALateLoan_ThenThrowsAnException(){
        //Given:
        CardReaderService cardReaderService = Mockito.mock(CardReaderService.class);
        UserService userService = Mockito.mock(UserService.class);
        LoanConfigurationProperties loanConfigurationProperties = new LoanConfigurationProperties();
        BookRepository bookRepository = Mockito.mock(BookRepository.class);
        LoanServiceImpl loanService = new LoanServiceImpl(cardReaderService,userService,loanConfigurationProperties,bookRepository);

        User user = new User();
        Set<BooksByUser> borrowedBooks = new HashSet<>();
        BooksByUser booksByUser = new BooksByUser();
        LocalDate borrowDate = LocalDate.now().minusDays(45);
        String curp = "CURP";
        String []isbnList = {"HR835","MS375"};

        user.setCurp(curp);
        loanConfigurationProperties.setMaxBooksPerUser(15);
        booksByUser.setCurp(curp);
        booksByUser.setIsbn("SB129");
        booksByUser.setBorrowDate(borrowDate);

        borrowedBooks.add(booksByUser);

        Mockito.when(userService.getBorrowedBooks(curp)).thenReturn(borrowedBooks);

        //When:

        //Then:
        assertThatThrownBy(() -> loanService.validateLoan(user,isbnList)).isInstanceOf(LoanNotAllowedException.class).hasMessage("User must return old loans");

    }

    @Test
    public void GivenAnUserWithTheSameLoan_ThenThrowsAnException(){
        //Given:
        CardReaderService cardReaderService = Mockito.mock(CardReaderService.class);
        UserService userService = Mockito.mock(UserService.class);
        LoanConfigurationProperties loanConfigurationProperties = new LoanConfigurationProperties();
        BookRepository bookRepository = Mockito.mock(BookRepository.class);
        LoanServiceImpl loanService = new LoanServiceImpl(cardReaderService,userService,loanConfigurationProperties,bookRepository);

        User user = new User();
        Set<BooksByUser> borrowedBooks = new HashSet<>();
        BooksByUser booksByUser = new BooksByUser();
        LocalDate borrowDate = LocalDate.now();
        String curp = "CURP";
        String []isbnList = {"HR835","SB129"};

        user.setCurp(curp);
        loanConfigurationProperties.setMaxBooksPerUser(15);
        booksByUser.setCurp(curp);
        booksByUser.setIsbn("SB129");
        booksByUser.setBorrowDate(borrowDate);

        borrowedBooks.add(booksByUser);

        Mockito.when(userService.getBorrowedBooks(curp)).thenReturn(borrowedBooks);

        //When:

        //Then:
        assertThatThrownBy(() -> loanService.validateLoan(user,isbnList)).isInstanceOf(LoanNotAllowedException.class).hasMessage("Attempt to borrow more than once a book");

    }

    @Test
    public void GivenAnUserWithTheMaxNumberOfLoansAllowed_ThenThrowsAnException(){
        //Given:
        CardReaderService cardReaderService = Mockito.mock(CardReaderService.class);
        UserService userService = Mockito.mock(UserService.class);
        LoanConfigurationProperties loanConfigurationProperties = new LoanConfigurationProperties();
        BookRepository bookRepository = Mockito.mock(BookRepository.class);
        LoanServiceImpl loanService = new LoanServiceImpl(cardReaderService,userService,loanConfigurationProperties,bookRepository);

        User user = new User();
        Set<BooksByUser> borrowedBooks = new HashSet<>();
        BooksByUser booksByUserA = new BooksByUser();
        BooksByUser booksByUserB = new BooksByUser();
        BooksByUser booksByUserC = new BooksByUser();
        LocalDate borrowDate = LocalDate.now();
        String curp = "CURP";
        String []isbnList = {};

        user.setCurp(curp);
        loanConfigurationProperties.setMaxBooksPerUser(2);
        booksByUserA.setCurp(curp);
        booksByUserA.setBorrowDate(borrowDate);
        booksByUserA.setIsbn("SB129");
        booksByUserB.setCurp(curp);
        booksByUserB.setBorrowDate(borrowDate);
        booksByUserB.setIsbn("EK483");
        booksByUserC.setCurp(curp);
        booksByUserC.setBorrowDate(borrowDate);
        booksByUserC.setIsbn("UG385");

        borrowedBooks.add(booksByUserA);
        borrowedBooks.add(booksByUserB);
        borrowedBooks.add(booksByUserC);

        Mockito.when(userService.getBorrowedBooks(curp)).thenReturn(borrowedBooks);

        //When:

        //Then:
        assertThatThrownBy(() -> loanService.validateLoan(user,isbnList)).isInstanceOf(LoanNotAllowedException.class).hasMessage("User has reached the max number of books allowed");

    }
}
