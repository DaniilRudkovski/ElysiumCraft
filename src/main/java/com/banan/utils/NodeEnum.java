package com.banan.utils;

public enum NodeEnum {
    // 1. Сначала создаем ВСЕ константы (связи пока null)
    MAIN_MENU(
        "Меню",0, 0, 0, null, null),
    SETTINGS(
        "Настройки",1, 0, 0, null, null),
    SPAWN(
        "Спавн мобов",2, 0, 0, null, null),
    SPAWNER(
        "рассадник мобов",3, 0, 2, null, null),
    TRIAL(
        "рассадник испытаний",3, 1, 2, null, null),
    NATURAL_SPAWN(
        "обычный спавн",3, 2, 2, null, null),
    FUNCTIONS(
        "Функции",1, 1, 0, null, null),
    UPDATE_SPAWNERS(
        "обновить спавнера",2, 0, 1, null, null),
    GO_BACK(
        "назад",-1, -1, 0, null, null);
    
    public String desc;//описание кнопки(текст)
    public int menuLayer;//уровень в меню на котором будет отображаться опция
    public int posInLayer;//очерендность опции в рамках уровня меню
    public int nodeType;//0 - просто проходная нода, 1 - кнопка активирующая что-то , 2 - кнопка переключатель между вкл выкл
    public NodeEnum uppernode;
    public NodeEnum[] underNodes;

    NodeEnum(String desc,int menuLayer, int posInLayer, int nodeType, NodeEnum uppernode, NodeEnum[] underNodes) {
        this.desc = desc;
        this.menuLayer = menuLayer;
        this.posInLayer = posInLayer;
        this.nodeType = nodeType;
        this.uppernode = uppernode;
        this.underNodes = underNodes;
    }

    // 2. А вот здесь (когда все константы уже созданы) мы связываем их
    static {
        MAIN_MENU.underNodes = new NodeEnum[]{SETTINGS, FUNCTIONS};
        
        SETTINGS.uppernode = MAIN_MENU;
        SETTINGS.underNodes = new NodeEnum[]{SPAWN};
        
        SPAWN.uppernode = SETTINGS;
        SPAWN.underNodes = new NodeEnum[]{SPAWNER, TRIAL, NATURAL_SPAWN};
        
        SPAWNER.uppernode = SPAWN;
        TRIAL.uppernode = SPAWN;
        NATURAL_SPAWN.uppernode = SPAWN;
        
        FUNCTIONS.uppernode = MAIN_MENU;
        FUNCTIONS.underNodes = new NodeEnum[]{UPDATE_SPAWNERS};
        
        UPDATE_SPAWNERS.uppernode = FUNCTIONS;
    }

    /* струкутура меню
        /elysium (меню)
            меню
                /elysium settings
                настройки
                    /elysium settings spawn
                    спавн(тут вкл выкл на каждую опцию прямо в сообщении кнопку 1 обновлять)
                        рассадник мобов
                        рассадник испытаний - спавнер
                        обычный спавн
                /elisium functions
                функции
                    /elisium functions updatespawners
                    обновить спавнера */
}