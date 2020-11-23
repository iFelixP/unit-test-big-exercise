package com.nearsoft.training.library.service.impl;

import com.nearsoft.training.library.config.CardReaderConfigurationProperties;
import com.nearsoft.training.library.model.User;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import java.io.*;

public class CardReaderServiceImplTest {

    @Test
    public void readUser() throws IOException {
        //Given:
        CardReaderConfigurationProperties cardReaderConfigurationProperties = Mockito.mock(CardReaderConfigurationProperties.class);
        CardReaderServiceImpl cardReaderService = new CardReaderServiceImpl(cardReaderConfigurationProperties);
        InputStream inputStream = Mockito.mock(InputStream.class);
        OutputStream outputStream = Mockito.mock(OutputStream.class);

        BufferedReader in = Mockito.mock(BufferedReader.class);
        String message = "NAME\\|CURP\\|DATE";

        Mockito.when(in.readLine()).thenReturn(message);

        //When:
        User userReturned = cardReaderService.getUser(outputStream,inputStream);

        //Then:
        assertEquals("NAME",userReturned.getName());
        assertEquals("CURP",userReturned.getCurp());
        assertEquals("DATE",userReturned.getValidityDate());
    }
}
