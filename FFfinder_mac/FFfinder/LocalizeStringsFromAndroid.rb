#!/usr/bin/env ruby

# gist: https://gist.github.com/3217498

# This script can be called from an Xcode 'Run Script' build phase at the
# beginning of the build process, like this:
#
#    ${PROJECT_DIR}/LocalizeStringsFromAndroid.rb ${PROJECT_NAME}
#
# This script should be placed in the same directory as your .xcodeproj
# project.

# By default, copy the res/values* dir tree from your Android project into
# your iOS project, and rename the res dir to android_res.

# Create the localizations in Xcode for your iOS project that you want this
# script to translate from the Android files.

require 'fileutils'
require 'pathname'
require 'rexml/document'
require 'iconv'

# Replace positional variable placeholders for strings with @'s.
def translate_placeholders(str)
  str.scan(/%\d\$s/i) { |p|
    str = str.gsub(/%\d\$s/i, p.gsub(/s/i, "@"))
  }
  str
end

def set_value_for_key_in_hash(value, key, hash)
  if hash.has_value?(value)
    # Start new keys array so we can sort them.
    keys = []
    keys_to_remove = []
    # Find existing keys for the values (should be only one)
    hash.map { |k, v|
      if v == value
        k.split(',').each { |sk|
          keys << sk.to_s
        }
        # Queue key to be removed
        keys_to_remove << k
      end
    }
    # Remove queued keys.
    keys_to_remove.each { |k|
      hash.delete k
    }
    # Add caller's key.
    keys << key
    # Concat and add key/value to hash
    key = ''
    keys.sort.each { |k|
      key += ',' unless key.length < 1
      key += k
    }
    hash[key] = value
  else
    hash[key] = value
  end
end

# The project path we are running in. Should be the project root directory.
project_path = Pathname.pwd
project_name = ARGV.first

# The path to Android's res directory, where the values-?? folders live.
res_path = project_path + "android_res"
unless res_path.exist?
  puts "Error! android_res directory not found: #{res_path}" 
  Process.exit!(true)
end

# Stuff base strings in here
base_strings = {}

# Loop thru the values-?? dirs in the res_path.
res_path.each_entry { |values_dir|
  next unless values_dir.fnmatch? 'values*'
  
  values_path = res_path + values_dir
  next unless (values_path + 'arrays.xml').exist? || (values_path + 'constants.xml').exist? || (values_path + 'strings.xml').exist?
  
  # Example values_dir paths:
  #   android_res/values
  #   android_res/values-cn
  #   android_res/values-en-rCA
  #   android_res/values-es-rMX
  
  # Set the base language to use.
  base_language = 'en.lproj'
  
  # Calculate the dest dir name.
  if values_dir.fnmatch? 'values'
    dest_dir = base_language
  else
    dest_dir = values_dir.to_s.gsub(/values-/, '').gsub(/-r/, '-') + '.lproj'
  end
  
  # Build the destination path.
  dest_path = project_path + project_name + dest_dir

  # If the dest dir does not exist, warn and continue.
  unless dest_path.exist?
    puts "Warning! iOS localization not set up for '#{dest_dir}'. Skipping."
    puts "Expected destination path: '#{dest_path.to_s}'"
    next
  end
  
  # Stuff the strings in here.
  strings = {}
  
  # Process the arrays.xml, constants.xml, and strings.xml files.
  %w[arrays.xml constants.xml strings.xml].each { |src_file|
    src_path = values_path + src_file
    next unless src_path.exist?
    
    xml = File.read src_path.to_s
    doc = REXML::Document.new(xml)
    
    # Process array string elements.
    doc.elements.each('resources/string-array') { |arr|
      key = arr.attributes['name']

      if arr.has_elements?
        arr_index = 0
        arr.elements.each { |e|
          if e.name = 'item'
            value = translate_placeholders(e.text)
            item_key = "#{key}-#{arr_index}"
            
            set_value_for_key_in_hash(value, item_key, base_strings) if dest_dir == base_language
            set_value_for_key_in_hash(value, item_key, strings)

            arr_index += 1
          end
        }
      end
    }
    
    # Process non-array string elements.
    doc.elements.each('resources/string') { |str|
      key = str.attributes['name']
      
      # Look for <a><u>Value</u></a> sub elements
      until str.has_elements? == false
        str.each_element { |astr|
          str = astr
        }
      end
      value = translate_placeholders(str.text)
      
      set_value_for_key_in_hash(value, key, base_strings) if dest_dir == base_language
      set_value_for_key_in_hash(value, key, strings)
    }
  }
  
  # If Localizable.strings exists, remove it.
  loc_str_path = dest_path + 'Localizable.strings'
  loc_str_path.delete if loc_str_path.exist?
  
  # Write the new Localizable.strings file.
  converter = Iconv.new 'utf-16le', ''
  File.open(loc_str_path, 'wb') { |f|
    f.write 0xff.chr
    f.write 0xfe.chr
    
    #base_strings.keys.sort.each { |key|
    base_strings.sort_by { |k,v| v }.each { |key, value|
      base_value = base_strings[key]
      value = strings.keys.include?(key) ? strings[key] : base_value
      
      value = value.gsub(/\n/, ' ')
      
      unless key.include? ','
        base_value = key if key.match(/-\d+\z/)
      end
      
      f.write converter.iconv "/*@ #{key} */\n"
      f.write converter.iconv "\"#{key}\" = \"#{value}\";\n\n"
    }
  }
}
