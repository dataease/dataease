<template>
	<el-form size='small'>
		<el-form-item>
			<el-radio v-model='radioValue' :label="1">
        {{$t('schedule.cron.weeks')}}，{{$t('schedule.cron.weeks_allowed_wildcards')}}
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="2">
        {{$t('schedule.cron.not_specify')}}
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="3">
        {{$t('schedule.cron.period')}} {{$t('schedule.cron.from')}}{{$t('schedule.cron.week')}}
				<el-input-number v-model='cycle01' :min="1" :max="7" /> -
				<el-input-number v-model='cycle02' :min="1" :max="7" />
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="4">
				{{$t('schedule.cron.num')}}
				<el-input-number v-model='average01' :min="1" :max="4" /> {{$t('schedule.cron.week_of_weeks')}}
				<el-input-number v-model='average02' :min="1" :max="7" />
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="5">
        {{$t('schedule.cron.last_week_of_the_month')}}
				<el-input-number v-model='weekday' :min="1" :max="7" />
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="6">
        {{$t('schedule.cron.specify')}}
        <el-select clearable v-model="checkboxList" :placeholder="$t('schedule.cron.multi_select')" multiple style="width:100%">
					<el-option v-for="(item,index) of weekList" :key="index" :value="index+1">{{item}}</el-option>
				</el-select>
			</el-radio>
		</el-form-item>

	</el-form>
</template>

<script>
export default {
	data() {
		return {
			radioValue: 2,
			weekday: 1,
			cycle01: 1,
			cycle02: 2,
			average01: 1,
			average02: 1,
			checkboxList: [],
			weekList: [this.$t('commons.weeks_1'),
        this.$t('commons.weeks_2'),
        this.$t('commons.weeks_3'),
        this.$t('commons.weeks_4'),
        this.$t('commons.weeks_5'),
        this.$t('commons.weeks_6'),
        this.$t('commons.weeks_0'),
      ],
			checkNum: this.$options.propsData.check
		}
	},
	name: 'CrontabWeek',
	props: ['check', 'cron'],
	methods: {
		// 单选按钮值变化时
		radioChange() {
			if (this.radioValue === 1) {
				this.$emit('update', 'week', '*');
				this.$emit('update', 'year', '*');
			} else {
				if (this.cron.mouth === '*') {
					this.$emit('update', 'mouth', '1', 'week');
				}
				if (this.cron.day === '*') {
					this.$emit('update', 'day', '1', 'week');
				}
				if (this.cron.hour === '*') {
					this.$emit('update', 'hour', '0', 'week');
				}
				if (this.cron.min === '*') {
					this.$emit('update', 'min', '0', 'week');
				}
				if (this.cron.second === '*') {
					this.$emit('update', 'second', '0', 'week');
				}
			}
			switch (this.radioValue) {
				case 2:
					this.$emit('update', 'week', '?');
					break;
				case 3:
					this.$emit('update', 'week', this.cycle01 + '-' + this.cycle02);
					break;
				case 4:
					this.$emit('update', 'week', this.average01 + '#' + this.average02);
					break;
				case 5:
					this.$emit('update', 'week', this.weekday + 'L');
					break;
				case 6:
					this.$emit('update', 'week', this.checkboxString);
					break;
			}
		},
		// 根据互斥事件，更改radio的值

		// 周期两个值变化时
		cycleChange() {
			if (this.radioValue == '3') {
				this.$emit('update', 'week', this.cycleTotal);
			}
		},
		// 平均两个值变化时
		averageChange() {
			if (this.radioValue == '4') {
				this.$emit('update', 'week', this.averageTotal);
			}
		},
		// 最近工作日值变化时
		weekdayChange() {
			if (this.radioValue == '5') {
				this.$emit('update', 'week', this.weekday + 'L');
			}
		},
		// checkbox值变化时
		checkboxChange() {
			if (this.radioValue == '6') {
				this.$emit('update', 'week', this.checkboxString);
			}
		},
	},
	watch: {
		"radioValue": "radioChange",
		'cycleTotal': 'cycleChange',
		'averageTotal': 'averageChange',
		'weekdayCheck': 'weekdayChange',
		'checkboxString': 'checkboxChange',
	},
	computed: {
		// 计算两个周期值
		cycleTotal: function () {
			this.checkNum(this.cycle01, 1, 7)
			this.checkNum(this.cycle02, 1, 7)
			return this.cycle01 + '-' + this.cycle02;
		},
		// 计算平均用到的值
		averageTotal: function () {
			this.checkNum(this.average01, 1, 4)
			this.checkNum(this.average02, 1, 7)
			return this.average01 + '#' + this.average02;
		},
		// 最近的工作日（格式）
		weekdayCheck: function () {
			this.checkNum(this.weekday, 1, 7)
			return this.weekday;
		},
		// 计算勾选的checkbox值合集
		checkboxString: function () {
			let str = this.checkboxList.join();
			return str == '' ? '?' : str;
		}
	}
}
</script>
