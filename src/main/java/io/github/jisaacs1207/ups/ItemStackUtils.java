package io.github.jisaacs1207.ups;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
 
 
public final  class ItemStackUtils {
                public static String getEnchants(ItemStack i){
                        List<String> e = new ArrayList<String>();
                        Map<Enchantment, Integer> en = i.getEnchantments();
                        for(Enchantment t : en.keySet()) {
                                e.add(t.getName() + ":" +en.get(t));
                        }
                        return StringUtils.join(e, ",");
                }
                public static String deserialize (ItemStack i){
                  String[] parts = new String[6];
                  parts[0] = i.getType().name();
                  parts [1] = Integer.toString(i.getAmount());
                  parts [2] = String.valueOf(i.getDurability());
                  parts [3] = i.getItemMeta().getDisplayName();
                  parts [4] = String.valueOf (i.getData().getData());
                  parts [5] = getEnchants(i);
                  return StringUtils.join(parts, ";");
 
                }
                public static ItemStack deserial(String p){
               
                String[] a = p.split(";");
                ItemStack i = new ItemStack(Material.getMaterial(a[0]), Integer.parseInt (a [1]));
                i.setDurability((short) Integer.parseInt(a [2]));
                ItemMeta meta = i.getItemMeta();
                meta.setDisplayName(a[3]);
                i.setItemMeta(meta);
                MaterialData data = i.getData();
                data.setData ((byte) Integer.parseInt (a[4]));
                i.setData(data);
                if (a.length > 5) {
                        String[] parts = a[5].split(",");
                        for(String s : parts) {
                                String label = s.split(":")[0];
                                String amplifier = s.split(":")[1];
                                Enchantment type = Enchantment.getByName(label);
                                if(type == null) {
                                        continue;
                                }
                                int f;
                                try {
                                        f = Integer.parseInt(amplifier);
                                }catch(Exception ex) {
                                        continue;
                                }
                                i.addEnchantment(type, f);
                        }
                }
                return i;
                }
               
}