package com.otobus.service;

import com.otobus.entity.Company;
import com.otobus.repository.CompanyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Firma servisi.
 * Firma CRUD işlemlerini yönetir.
 */
@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    public Company getCompanyById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Firma bulunamadı!"));
    }

    public Company createCompany(Company company) {
        return companyRepository.save(company);
    }

    public Company updateCompany(Long id, String name) {
        Company company = getCompanyById(id);
        company.setName(name);
        return companyRepository.save(company);
    }

    public void deleteCompany(Long id) {
        if (!companyRepository.existsById(id)) {
            throw new RuntimeException("Firma bulunamadı!");
        }
        companyRepository.deleteById(id);
    }
}
