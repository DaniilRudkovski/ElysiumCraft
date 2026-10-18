package com.banan.modules.Spawn;

import com.banan.modules.Rule;
import org.bukkit.event.Event;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class SpawnRule extends Rule {

    @Override
    public void handleEvent(Event event) {
        // По умолчанию разрешаем (true = можно)
        this.isEventApplied = true;

        if (!isActive()) {
            return;
        }

        if (!(event instanceof CreatureSpawnEvent spawnEvent)) {
            return;
        }

        SpawnReason reason = spawnEvent.getSpawnReason();

        // Разрешаем спавнеры и рассадники (мы не хотим, чтобы их отменяли)
        if (reason == SpawnReason.SPAWNER || reason == SpawnReason.TRIAL_SPAWNER) {
            return; // isEventApplied остается true
        }

        // Запрещаем естественный спавн (isEventApplied = false)
        if (reason == SpawnReason.NATURAL) {
            this.isEventApplied = false;
        }
    }

    @Override 
    public boolean isEventApplied() {
        return this.isEventApplied;
    }
}