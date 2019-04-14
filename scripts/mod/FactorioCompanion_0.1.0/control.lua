script.on_event({defines.events.on_tick},
   function (e)
      if e.tick % (60 * 5) == 0 then -- every minute
         game.print("Statistacks done!")

         if not global.statistics then
            global.statistics = {}
         end

         if not global.itemStats then
            global.itemStats = {}
         end

         if not global.itemStatsPrev then
            global.itemStatsPrev = {}
         end

         for name, count in pairs(game.forces["player"].item_production_statistics.output_counts) do
            if name and count then 
               if not global.itemStats[name] then 
                  global.itemStats[name] = count
               end

               global.itemStatsPrev[name] = global.itemStats[name]
               global.itemStats[name] = count

               local stats = {}
               stats.name = name
               stats.count = count
               stats.rate = global.itemStats[name] - global.itemStatsPrev[name]

               global.statistics[name] = stats
            end
         end

         for name, count in pairs(game.forces["player"].fluid_production_statistics.output_counts) do
            if name and count then 
               if not global.itemStats[name] then 
                  global.itemStats[name] = count
               end

               global.itemStatsPrev[name] = global.itemStats[name]
               global.itemStats[name] = count

               local stats = {}
               stats.name = name
               stats.count = count
               stats.rate = global.itemStats[name] - global.itemStatsPrev[name]

               global.statistics[name] = stats
            end
         end

         itemStatsDump = game.table_to_json(global.statistics)
         game.write_file("itemStatistacks.txt", itemStatsDump, false, 1)

         for index,player in pairs(game.connected_players) do
            game.take_screenshot{player = player, path = "imaj_" .. player.name .. ".jpg", resolution = {8192, 8192}, anti_alias = true}
         end
      end
   end
)
