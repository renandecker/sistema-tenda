package br.com.sistematenda.pdv.repository;

import br.com.sistematenda.pdv.entity.MovimentoCaixa;
import br.com.sistematenda.pdv.entity.SessaoCaixa;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class MovimentoCaixaRepository implements PanacheRepositoryBase<MovimentoCaixa, UUID> {

    public List<MovimentoCaixa> findBySessao(SessaoCaixa sessao) {
        return find("sessaoCaixa = ?1 and deleted = false order by data", sessao).list();
    }

    public List<MovimentoCaixa> findByTipo(MovimentoCaixa.TipoMovimento tipo) {
        return find("tipo = ?1 and deleted = false", tipo).list();
    }

    public List<MovimentoCaixa> findByPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return find("data >= ?1 and data <= ?2 and deleted = false", inicio, fim).list();
    }

    public MovimentoCaixa findUltimaSangria(SessaoCaixa sessao) {
        return find("sessaoCaixa = ?1 and tipo = 'SANGRIA' and deleted = false order by data desc", sessao)
            .firstResult();
    }

    public MovimentoCaixa findUltimoReforco(SessaoCaixa sessao) {
        return find("sessaoCaixa = ?1 and tipo = 'REFORCO' and deleted = false order by data desc", sessao)
            .firstResult();
    }

    public java.math.BigDecimal getTotalByTipoPeriodo(MovimentoCaixa.TipoMovimento tipo,
                                                     LocalDateTime inicio, LocalDateTime fim) {
        return find("select sum(valor) from MovimentoCaixa where tipo = ?1 and data >= ?2 and data <= ?3 and deleted = false", tipo, inicio, fim)
            .project(java.math.BigDecimal.class)
            .firstResultOptional()
            .orElse(java.math.BigDecimal.ZERO);
    }

    public java.math.BigDecimal getTotalSangriasSessao(SessaoCaixa sessao) {
        return find("select sum(valor) from MovimentoCaixa where sessaoCaixa = ?1 and tipo = 'SANGRIA' and deleted = false", sessao)
            .project(java.math.BigDecimal.class)
            .firstResultOptional()
            .orElse(java.math.BigDecimal.ZERO);
    }

    public java.math.BigDecimal getTotalReforcosSessao(SessaoCaixa sessao) {
        return find("select sum(valor) from MovimentoCaixa where sessaoCaixa = ?1 and tipo = 'REFORCO' and deleted = false", sessao)
            .project(java.math.BigDecimal.class)
            .firstResultOptional()
            .orElse(java.math.BigDecimal.ZERO);
    }
}