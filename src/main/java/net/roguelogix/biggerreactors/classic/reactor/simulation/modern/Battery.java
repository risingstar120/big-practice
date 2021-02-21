package net.roguelogix.biggerreactors.classic.reactor.simulation.modern;

import net.minecraft.nbt.CompoundNBT;
import net.roguelogix.biggerreactors.Config;
import net.roguelogix.biggerreactors.classic.reactor.simulation.IReactorBattery;
import net.roguelogix.phosphophyllite.util.HeatBody;

public class Battery extends HeatBody implements IReactorBattery {
    private long capacity;
    private long stored;
    private long generatedLastTick;
    
    {
        setInfinite(true);
    }
    
    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }
    
    @Override
    public double transferWith(HeatBody other, double rfkt) {
        double newTemp = other.temperature() - temperature();
        newTemp *= Math.exp(-rfkt / other.rfPerKelvin());
        newTemp += temperature();
        
        double rfTransferred = (newTemp - other.temperature()) * other.rfPerKelvin();
    
        generatedLastTick = (long) (-rfTransferred * Config.Reactor.OutputMultiplier * Config.Reactor.PassiveOutputMultiplier);
        stored += generatedLastTick;
        if(stored > capacity){
            stored = capacity;
        }
        
        other.setTemperature(newTemp);
        
        return rfTransferred;
    }
    
    @Override
    public long extract(long toExtract) {
        return 0;
    }
    
    @Override
    public long stored() {
        return stored;
    }
    
    @Override
    public long capacity() {
        return capacity;
    }
    
    @Override
    public CompoundNBT serializeNBT() {
        return null;
    }
    
    @Override
    public void deserializeNBT(CompoundNBT nbt) {
    
    }
    
    public long generatedLastTick() {
        return generatedLastTick;
    }
}
