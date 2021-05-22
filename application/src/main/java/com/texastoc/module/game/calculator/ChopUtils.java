package com.texastoc.module.game.calculator;

import java.util.List;

public class ChopUtils {

  public static void adjustTotal(int total, List<Integer> values) {
    int totalValue = 0;
    for (Integer value : values) {
      totalValue += value;
    }

    int index = 0;
    if (total < totalValue) {
      index = values.size() - 1;
    }

    while (total != totalValue) {
      if (total > totalValue) {
        values.set(index, values.get(index) + 1);
        ++totalValue;
        if (++index == values.size()) {
          index = 0;
        }
      } else {
        values.set(index, values.get(index) - 1);
        --totalValue;
        if (--index < 0) {
          index = values.size() - 1;
        }
      }
    }

  }
}
