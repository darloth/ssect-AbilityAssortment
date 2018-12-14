package darloth.abilityassortment.campaign.abilities;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.abilities.BaseToggleAbility;
import com.fs.starfarer.api.util.Misc;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: Darloth
 * Date: 13/12/2018
 * Time: 11:10
 */
public class LongTermBerth extends BaseToggleAbility
{

    @Override
    protected String getActivationText() {
        return "Entering long-term holding pattern";
    }

    @Override
    public boolean isUsable()
    {
        final CampaignFleetAPI fleet = getFleet();
        //if(fleet.getOrbit() == null) return false; // don't think we need?
        if(fleet.getOrbitFocus() == null) return false;
        if(fleet.getOrbitFocus().getMarket() == null) return false;
        FactionAPI orbitedFaction = fleet.getOrbitFocus().getMarket().getFaction();

        if(orbitedFaction == null) return false;
        if(orbitedFaction.getRelToPlayer().isHostile()) return false;

        if(fleet.getFleetData().areAnyShipsPerformingRepairs()) return false;

        return super.isUsable();
    }

    @Override
    protected void activateImpl()
    {
    }

    // TODO: Tooltip!

    // TODO: Icon!

    @Override
    protected void applyEffect(float amount, float level)
    {
        // guard against fleet being null somehow.
        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) return;

        if (level > 0) level = 1; // binary, it's either working or it isn't.

        List<FleetMemberAPI> fleetMembers = fleet.getFleetData().getMembersListCopy();
        for(FleetMemberAPI member : fleetMembers)
        {
            member.getStats().getSuppliesPerMonth().modifyMult(id, 0f);
        }


        if (level <= 0) {
            cleanupImpl();
        }
    }

    @Override
    public void advance(float amount)
    {
        super.advance(amount);
        if(this.isActive()) {
            if (!this.isUsable()) {
                this.deactivate(); // turn if off if any of the conditions for use become false.
            }
        }
    }

    // Copied from BaseToggleAbility.deactivate() to get around potential bug or otherwise unwanted behaviour
    // noted here: http://fractalsoftworks.com/forum/index.php?topic=14678.0
    @Override
    public void deactivate() {
        if (isActive()) {
            turnedOn = false;
            if (entity.isInCurrentLocation() && entity.isVisibleToPlayerFleet() && !entity.isPlayerFleet()) {
                String soundId = getOffSoundWorld();
                if (soundId != null) {
                    Global.getSoundPlayer().playSound(soundId, 1f, 1f, entity.getLocation(), entity.getVelocity());
                }
            }
            if (getDeactivationDays() <= 0) {
                level = 0;
            }
            cooldownLeft = getDeactivateCooldownDays();
            isActivateCooldown = false;

            applyEffect(0f, level);

            if (entity.isInCurrentLocation()) {
                if (getDeactivationText() != null && entity.isVisibleToPlayerFleet()) {
                    entity.addFloatingText(getDeactivationText(), Misc.setAlpha(entity.getIndicatorColor(), 255), 0.5f);
                }
            }
            deactivateImpl();

            super.deactivate();
        }
    }

    @Override
    protected void deactivateImpl()
    {

    }

    @Override
    protected void cleanupImpl()
    {
        // guard against fleet being null somehow.
        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) return;

        // unapply modification
        List<FleetMemberAPI> fleetMembers = fleet.getFleetData().getMembersListCopy();
        for(FleetMemberAPI member : fleetMembers)
        {
            member.getStats().getSuppliesPerMonth().unmodify(id);
        }
    }
}
