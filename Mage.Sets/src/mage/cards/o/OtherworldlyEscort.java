package mage.cards.o;

import java.util.UUID;
import mage.MageInt;
import mage.abilities.keyword.FlashAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.Duration;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.DiesSourceTriggeredAbility;
import mage.abilities.effects.common.DestroyTargetEffect;
import mage.abilities.effects.common.ReturnFromGraveyardToBattlefieldWithCounterTargetEffect;
import mage.abilities.effects.common.continuous.AddCardSubTypeTargetEffect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.condition.Condition;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.counters.CounterType;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.TargetPermanent;
import mage.target.targetpointer.FixedTarget;
import mage.util.CardUtil;
import mage.constants.TargetController;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.other.DamagedPlayerThisTurnPredicate;

/**
 *
 * @author Kilo
 */
public final class OtherworldlyEscort extends CardImpl {

    private static final FilterCreaturePermanent filter = new FilterCreaturePermanent("creature that dealt damage to you this turn");

    static {
        filter.add(new DamagedPlayerThisTurnPredicate(TargetController.YOU));
    }

    public OtherworldlyEscort(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{3}{W}");
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.DETECTIVE);

        this.power = new MageInt(4);
        this.toughness = new MageInt(3);

        // Otherworldly Escort
        // {3}{W}
        // Creature — Human Detective
        // Flash
        // When this creature dies, if it’s not a Spirit, return it to the battlefield under its owner’s control with four charge counters on it. It’s a Spirit Detective. (It’s no longer a Human.)
        // {1}{W}, {T}, Remove a charge counter from this creature: Destroy target creature that dealt damage to you this turn.
        // 4/3

        // Flash
        this.addAbility(FlashAbility.getInstance());

        // When this creature dies, if it’s not a Spirit, return it to the battlefield under its owner’s control with four charge counters on it. It’s a Spirit Detective. (It’s no longer a Human.)
        this.addAbility(new DiesSourceTriggeredAbility(new OtherworldlyEscortEffect())
                .withInterveningIf(OtherworldlyEscortCondition.instance));

        // {1}{W}, {T}, Remove a charge counter from this creature: Destroy target creature that dealt damage to you this turn.
        Ability ability = new SimpleActivatedAbility(new DestroyTargetEffect(), new ManaCostsImpl<>("{1}{W}"));
        ability.addCost(new TapSourceCost());
        ability.addCost(new RemoveCountersSourceCost(CounterType.CHARGE.createInstance(1)));
        ability.addTarget(new TargetPermanent(filter));
        this.addAbility(ability);
    }

    private OtherworldlyEscort(final OtherworldlyEscort card) {
        super(card);
    }

    @Override
    public OtherworldlyEscort copy() {
        return new OtherworldlyEscort(this);
    }
}

enum OtherworldlyEscortCondition implements Condition {
    instance;

    @Override
    public boolean apply(Game game, Ability source) {
        return CardUtil
                .getEffectValueFromAbility(source, "permanentLeftBattlefield", Permanent.class)
                .filter(permanent -> !permanent.hasSubtype(SubType.SPIRIT, game))
                .isPresent();
    }

    @Override
    public String toString() {
        return "it’s not a Spirit";
    }
}

class OtherworldlyEscortEffect extends OneShotEffect {

    OtherworldlyEscortEffect() {
        super(Outcome.PutCreatureInPlay);
        staticText = "return it to the battlefield under its owner's control with four charge counters on it. It's a Spirit Detective. (It's no longer a Human.)";
    }

    private OtherworldlyEscortEffect(final OtherworldlyEscortEffect effect) {
        super(effect);
    }

    @Override
    public OtherworldlyEscortEffect copy() {
        return new OtherworldlyEscortEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null || game.getState().getZone(source.getSourceId()) != Zone.GRAVEYARD) {
            return false;
        }
        Card card = game.getCard(source.getSourceId());
        if (card == null) {
            return false;
        }
        OneShotEffect effect = new ReturnFromGraveyardToBattlefieldWithCounterTargetEffect(
                CounterType.CHARGE.createInstance(4)
        );
        effect.setTargetPointer(new FixedTarget(card, game));
        effect.apply(game, source);
        game.processAction();
        Permanent permanent = CardUtil.getPermanentFromCardPutToBattlefield(card, game);
        if (permanent != null) {
            permanent.removeSubType(game, SubType.HUMAN);
            game.addEffect(new AddCardSubTypeTargetEffect(SubType.SPIRIT, Duration.Custom).setTargetPointer(new FixedTarget(permanent, game)), source);
            game.addEffect(new AddCardSubTypeTargetEffect(SubType.DETECTIVE, Duration.Custom).setTargetPointer(new FixedTarget(permanent, game)), source);
        }
        return true;
    }
}
