package pl.madzierski.daniel.app.product_dict;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
class ProductDictService {

    private final ProductDictRepository productDictRepository;

}
