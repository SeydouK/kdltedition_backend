package com.kdlt.platform.quote.service;

import com.kdlt.platform.exceptions.BadRequestException;
import com.kdlt.platform.exceptions.ResourceNotFoundException;
import com.kdlt.platform.product.entity.Product;
import com.kdlt.platform.product.entity.ProductType;
import com.kdlt.platform.product.repository.ProductRepository;
import com.kdlt.platform.quote.dto.CreateQuoteDto;
import com.kdlt.platform.quote.dto.QuoteDto;
import com.kdlt.platform.quote.dto.RespondQuoteDto;
import com.kdlt.platform.quote.entity.Quote;
import com.kdlt.platform.quote.entity.QuoteStatus;
import com.kdlt.platform.quote.repository.QuoteRepository;
import com.kdlt.platform.user.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final ProductRepository productRepository;

    public QuoteService(QuoteRepository quoteRepository, ProductRepository productRepository) {
        this.quoteRepository = quoteRepository;
        this.productRepository = productRepository;
    }

    public QuoteDto createQuote(User user, CreateQuoteDto dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable."));

        if (product.getType() != ProductType.CUSTOM) {
            throw new BadRequestException("Ce produit a un prix fixe, utilisez le panier plutôt qu'une demande de devis.");
        }

        Quote quote = new Quote();
        quote.setUser(user);
        quote.setProduct(product);
        quote.setQuantity(dto.getQuantity());
        quote.setSpecifications(dto.getSpecifications());
        quote.setStatus(QuoteStatus.PENDING);

        Quote saved = quoteRepository.save(quote);
        return mapToDto(saved);
    }

    public List<QuoteDto> getMyQuotes(User user) {
        return quoteRepository.findByUserIdOrderByDateCreationDesc(user.getId()).stream()
                .map(this::mapToDto)
                .toList();
    }

    public QuoteDto getMyQuote(User user, Long quoteId) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new ResourceNotFoundException("Devis introuvable."));

        if (!quote.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Devis introuvable.");
        }

        return mapToDto(quote);
    }

    public List<QuoteDto> getAllQuotes() {
        return quoteRepository.findAllByOrderByDateCreationDesc().stream()
                .map(this::mapToDto)
                .toList();
    }

    public List<QuoteDto> getPendingQuotes() {
        return quoteRepository.findByStatusOrderByDateCreationDesc(QuoteStatus.PENDING).stream()
                .map(this::mapToDto)
                .toList();
    }

    public QuoteDto respondToQuote(Long quoteId, RespondQuoteDto dto) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new ResourceNotFoundException("Devis introuvable."));

        if (dto.getStatus() == QuoteStatus.PENDING) {
            throw new BadRequestException("Le statut de réponse doit être ANSWERED ou REJECTED.");
        }

        if (dto.getStatus() == QuoteStatus.ANSWERED && dto.getProposedPrice() == null) {
            throw new BadRequestException("Un prix proposé est requis pour répondre favorablement à un devis.");
        }

        quote.setStatus(dto.getStatus());
        quote.setProposedPrice(dto.getProposedPrice());
        quote.setStaffResponse(dto.getStaffResponse());

        Quote saved = quoteRepository.save(quote);
        return mapToDto(saved);
    }

    private QuoteDto mapToDto(Quote quote) {
        QuoteDto dto = new QuoteDto();
        dto.setId(quote.getId());
        dto.setProductId(quote.getProduct().getId());
        dto.setProductName(quote.getProduct().getName());
        dto.setQuantity(quote.getQuantity());
        dto.setSpecifications(quote.getSpecifications());
        dto.setStatus(quote.getStatus());
        dto.setProposedPrice(quote.getProposedPrice());
        dto.setStaffResponse(quote.getStaffResponse());
        dto.setDateCreation(quote.getDateCreation());
        dto.setCustomerEmail(quote.getUser().getEmail());
        dto.setCustomerName(quote.getUser().getFirstName() + " " + quote.getUser().getLastName());
        return dto;
    }
}