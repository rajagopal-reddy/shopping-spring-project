package com.trainingmug.service.cart;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.trainingmug.exceptions.ResourceNotFoundException;
import com.trainingmug.model.Cart;
import com.trainingmug.model.CartItem;
import com.trainingmug.model.Product;
import com.trainingmug.repository.CartIteamRepository;
import com.trainingmug.repository.CartRepository;
import com.trainingmug.service.product.IProductService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartItemService implements ICartItemService {
	
	private final CartIteamRepository cartIteamRepository;
	private final CartRepository cartRepository;
	private final IProductService productService;
	private final ICartService cartService;
	
	@Override
	public void addItemToCart(Long cartId, Long productId, int quantity) {
		// TODO Auto-generated method stub
		Cart cart = cartService.getCart(cartId);
		Product product = productService.getProductById(productId);
		CartItem cartItem = cart.getItems()
				.stream()
				.filter(item -> item.getProduct().getId().equals(productId))
				.findFirst()
				.orElse(new CartItem());
		if (cartItem.getId() == null) {
			cartItem.setCart(cart);
			cartItem.setProduct(product);
			cartItem.setQuantity(quantity);
		}
		else {
			cartItem.setQuantity(cartItem.getQuantity()+quantity);
		}
		cartItem.setTotalPrice();
		cart.addItem(cartItem);
		cartIteamRepository.save(cartItem);
		cartRepository.save(cart);
	}

	@Override
	public void removeItemFromCart(Long cartId, Long productId) {
		// TODO Auto-generated method stub
		Cart cart = cartService.getCart(cartId);
		CartItem itemToRemove  = getCartItem(cartId, productId);
		cart.removeItem(itemToRemove);
		cartRepository.save(cart);	
	}

	@Override
	public void updateItemQuantity(Long cartId, Long productId, int quantity) {
		Cart cart = cartService.getCart(cartId);
		cart.getItems()
		.stream()
		.filter(item -> item.getProduct().getId().equals(productId))
		.findFirst()
		.ifPresent(item -> {
			item.setQuantity(quantity);
			item.setUnitPrice(item.getProduct().getPrice());
			item.setTotalPrice();
		});
		BigDecimal totalAmount = cart.getTotalAmount();
		cart.setTotalAmount(totalAmount);
		cartRepository.save(cart);
	}
	
	@Override
	public CartItem getCartItem(Long cartId, Long productId) {
		Cart cart = cartService.getCart(cartId);
		return cart.getItems()
				.stream()
				.filter(item -> item.getProduct().getId().equals(productId))
				.findFirst()
				.orElseThrow(() -> new ResourceNotFoundException("Item not found !"));
	}

}
