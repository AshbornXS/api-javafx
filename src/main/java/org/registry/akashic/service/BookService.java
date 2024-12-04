package org.registry.akashic.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.registry.akashic.domain.Book;
import org.registry.akashic.exception.BadRequestException;
import org.registry.akashic.mapper.BookMapper;
import org.registry.akashic.repository.BookRepository;
import org.registry.akashic.requests.BookPostRequestBody;
import org.registry.akashic.requests.BookPutRequestBody;
import org.registry.akashic.util.ImageUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    public List<Book> listAll() {
        bookRepository.findAll().forEach(book -> book.setImageData(ImageUtil.decompressImage(book.getImageData())));
        return bookRepository.findAll();
    }

    public Book findByIdOrThrowBadRequestException(long id) {
        bookRepository.findAll().forEach(book -> book.setImageData(ImageUtil.decompressImage(book.getImageData())));
        return bookRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Book not Found"));
    }

    @Transactional
    public Book save(BookPostRequestBody bookPostRequestBody, MultipartFile imageFile) throws IOException {
        Book book = BookMapper.INSTANCE.toBook(bookPostRequestBody);
        if (imageFile != null && !imageFile.isEmpty()) {
            byte[] compressedImage = ImageUtil.compressImage(imageFile.getBytes());
            String imageName = book.getTitle().replaceAll("\\s+", "_") + ".jpg";
            book.setImageData(compressedImage);
            book.setImageName(imageName);
        }
        return bookRepository.save(book);
    }

    public Book replace(BookPutRequestBody bookPutRequestBody, MultipartFile imageFile) throws IOException {
        if (bookPutRequestBody.getId() == null) {
            throw new BadRequestException("Book ID cannot be null");
        }

        if (bookRepository.findById(bookPutRequestBody.getId()).isEmpty()) {
            throw new BadRequestException("Book not Found");
        }

        Book book = BookMapper.INSTANCE.toBook(bookPutRequestBody);
        log.warn("Updated Book: {}", book);
        log.warn("Book Put Request Body: {}", bookPutRequestBody);
        if (imageFile != null && !imageFile.isEmpty()) {
            byte[] compressedImage = ImageUtil.compressImage(imageFile.getBytes());
            String imageName = book.getTitle().replaceAll("\\s+", "_") + ".jpg";
            book.setImageData(compressedImage);
            book.setImageName(imageName);
        }
        return bookRepository.save(book);
    }

    public void delete(long id) {
        bookRepository.delete(bookRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Book not Found")));
    }

}
