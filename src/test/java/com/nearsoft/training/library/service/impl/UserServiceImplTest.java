package com.nearsoft.training.library.service.impl;


import com.nearsoft.training.library.model.BooksByUser;
import com.nearsoft.training.library.model.User;
import com.nearsoft.training.library.repository.BooksByUserRepository;
import com.nearsoft.training.library.repository.UserRepository;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertTrue;

public class UserServiceImplTest {

    @Test
    public void whenGetBorrowedBooks_thenBooksFromRepositoryAreReturned(){
        // Given:
        UserRepository userRepository = null;
        BooksByUserRepository otherRepository = Mockito.mock(BooksByUserRepository.class);
        UserServiceImpl userService = new UserServiceImpl(userRepository, otherRepository);
        String curp = "ABC";
        Set<BooksByUser> booksByUser = new HashSet<>();

        Mockito.when(otherRepository.findByCurp(curp)).thenReturn(booksByUser);

        // When:
        Set<BooksByUser> receivedBooksByUser = userService.getBorrowedBooks(curp);

        // Then:
        assertTrue(booksByUser == receivedBooksByUser);

        Mockito.verify(otherRepository).findByCurp(curp);
        Mockito.verifyNoMoreInteractions(otherRepository);
    }

    @Test
    public void WhenRegisterLoanWithAInexistentUserAndEmptyIsbnList_ThenOnlyNewUserIsAdded(){
        //Given:
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        BooksByUserRepository booksByUserRepository = Mockito.mock(BooksByUserRepository.class);
        UserServiceImpl userService = new UserServiceImpl(userRepository,booksByUserRepository);

        User user = new User();
        String []isbnList = {};
        String curp = "CURP";
        user.setCurp(curp);

        Mockito.when(userRepository.findById(curp)).thenReturn(Optional.empty());

        //When:
        userService.registerLoan(user,isbnList);

        //Then:
        Mockito.verify(userRepository).findById(curp);
        Mockito.verify(userRepository).save(user);
        Mockito.verifyNoMoreInteractions(userRepository,booksByUserRepository);
    }

    @Test
    public void WhenRegisterLoanAndIsbnIsDifferent_ThenLoanIsAdded() {
        //Given:
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        BooksByUserRepository booksByUserRepository = Mockito.mock(BooksByUserRepository.class);
        UserServiceImpl userService = new UserServiceImpl(userRepository, booksByUserRepository);

        User user = new User();
        String []isbnList ={"SP234","GH746","WK496"};
        String curp = "CURP";
        user.setCurp(curp);
        BooksByUser booksByUser = new BooksByUser();

        Mockito.when(userRepository.findById(curp)).thenReturn(Optional.of(user));
        Mockito.when(booksByUserRepository.findByIsbnAndCurp("SP234",curp)).thenReturn(Optional.of(booksByUser));
        Mockito.when(booksByUserRepository.findByIsbnAndCurp("GH746",curp)).thenReturn(Optional.of(booksByUser));
        Mockito.when(booksByUserRepository.findByIsbnAndCurp("WK496",curp)).thenReturn(Optional.empty());

        ArgumentCaptor<BooksByUser> captorBooksByUser = ArgumentCaptor.forClass(BooksByUser.class);

        //When:
        userService.registerLoan(user,isbnList);

        //Then:
        Mockito.verify(userRepository).findById(curp);
        Mockito.verify(booksByUserRepository).findByIsbnAndCurp("SP234",curp);
        Mockito.verify(booksByUserRepository).findByIsbnAndCurp("GH746",curp);
        Mockito.verify(booksByUserRepository).findByIsbnAndCurp("WK496",curp);
        Mockito.verify(booksByUserRepository).save(captorBooksByUser.capture());
        Mockito.verifyNoMoreInteractions(userRepository,booksByUserRepository);

        BooksByUser capturedBooksByUser = captorBooksByUser.getValue();

        assertTrue(capturedBooksByUser.getCurp().equals(curp));
        assertTrue(capturedBooksByUser.getIsbn().equals("WK496"));
    }

    @Test
    public void WhenRegisterReturnAndGiveAnInexistentUser_ThenCreateNewUserAndDeleteByCurpAndIsbn(){
        //Given:
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        BooksByUserRepository booksByUserRepository = Mockito.mock(BooksByUserRepository.class);
        UserServiceImpl userService = new UserServiceImpl(userRepository,booksByUserRepository);

        User user = new User();
        String []isbnList = {};
        String curp = "CURP";
        user.setCurp(curp);

        Mockito.when(userRepository.findById(curp)).thenReturn(Optional.empty());

        //When:
        userService.registerReturn(user,isbnList);

        //Then:
        Mockito.verify(userRepository).findById(curp);
        Mockito.verify(userRepository).save(user);
        Mockito.verify(booksByUserRepository).deleteByCurpAndIsbnIn(curp,isbnList);
        Mockito.verifyNoMoreInteractions(userRepository,booksByUserRepository);
    }
}
